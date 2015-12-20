/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jfxcreator.core.Program;
import jfxcreator.core.Project;
import net.sf.image4j.codec.ico.ICODecoder;

/**
 *
 * @author Aniket
 */
public class Viewer extends EnvironmentTab {

    private Image image;

    public Viewer(Program scr, Project pro) {
        super(scr, pro);
        if (scr.getFile().toAbsolutePath().toString().endsWith(".ico")) {
            image = null;
            try {
                image = SwingFXUtils.toFXImage(ICODecoder.read(scr.getFile().toFile()).get(0), null);
            } catch (Exception ex) {
            }
            if (image != null) {
                getCenter().setCenter(new ScrollPane(new ImageView(image)));
            }
        } else {
            getCenter().setCenter(new ScrollPane(new ImageView(image = new Image(scr.getFile().toUri().toString()))));
        }
    }

    public final Image getImage() {
        return image;
    }

}
