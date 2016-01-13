/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import jfxcreator.core.Program;
import jfxcreator.core.Project;
import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

/**
 *
 * @author Aniket
 */
public class Viewer extends EnvironmentTab {

    private final Image image;
    private final ToolBar controls;
    private final Button zIn, zOut, rotate, revert;
    private final ImageView view;
    private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);

    public Viewer(Program scr, Project pro) {
        super(scr, pro);
        getCenter().setCenter(new ScrollPane(view = new ImageView(image = readFromFile(scr.getFile()))));
        controls = new ToolBar();
        controls.setPadding(new Insets(5, 10, 5, 10));
        getCenter().setTop(controls);
        controls.getItems().addAll(zIn = new Button("Zoom In"),
                zOut = new Button("Zoom Out"),
                new Separator(),
                rotate = new Button("Rotate"),
                new Separator(),
                revert = new Button("Revert"));
        zIn.setOnAction((e) -> {
            if (zoom.get() < 2.0) {
                zoom.set(zoom.get() + 0.1);
            }
        });
        zOut.setOnAction((e) -> {
            if (zoom.get() > 0.2) {
                zoom.set(zoom.get() - 0.1);
            }
        });
        zoom.addListener((ob, older, newer) -> {
            view.setFitHeight(newer.doubleValue() * image.getHeight());
            view.setFitWidth(newer.doubleValue() * image.getWidth());
        });
        rotate.setOnAction((e) -> {
            view.setRotate(view.getRotate() + 90);
        });
        revert.setOnAction((e) -> {
            zoom.set(1.0);
            view.setRotate(0);
        });
    }

    private Image readFromFile(Path pr) {
        if (pr.toAbsolutePath().toString().endsWith(".ico")) {
            try {
                return SwingFXUtils.toFXImage(ICODecoder.read(pr.toFile()).get(0), null);
            } catch (IOException ex) {
            }
        }
        if (pr.toAbsolutePath().toString().endsWith(".wbmp")) {
            try {
                return SwingFXUtils.toFXImage(ImageIO.read(pr.toFile()), null);
            } catch (IOException ex) {
            }
        }
        if (isValidFxImage(pr.toFile())) {
            try {
                //GIF, JPEG, PNG, BMP
                return new Image(pr.toUri().toString());
            } catch (Exception e) {
            }
        }
        try {
            //icns, cur, pcx, dcx, tiff, psd, xpm, xbm
            return SwingFXUtils.toFXImage(imageReadExample(pr.toFile()), null);
        } catch (ImageReadException | IOException ex) {
        }
        return null;
    }

    public boolean isValidFxImage(File f) {
        String s = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1);
        return s.equalsIgnoreCase("gif")
                || s.equalsIgnoreCase("jpg")
                || s.equalsIgnoreCase("jpeg")
                || s.equalsIgnoreCase("png")
                || s.equalsIgnoreCase("bmp");
    }

    public static BufferedImage imageReadExample(final File file)
            throws ImageReadException, IOException {
        final BufferedImage image = Imaging.getBufferedImage(file);
        return image;
    }

    public final Image getImage() {
        return image;
    }

}
