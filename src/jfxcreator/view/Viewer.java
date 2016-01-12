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
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ScrollPane;
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

    public Viewer(Program scr, Project pro) {
        super(scr, pro);
        getCenter().setCenter(new ScrollPane(new ImageView(image = readFromFile(scr.getFile()))));
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
                ||s.equalsIgnoreCase("jpg")
                ||s.equalsIgnoreCase("jpeg")
                ||s.equalsIgnoreCase("png")
                ||s.equalsIgnoreCase("bmp");
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
