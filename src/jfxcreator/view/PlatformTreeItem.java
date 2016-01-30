/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jfxcreator.core.JavaLibrary;
import jfxcreator.core.JavaPlatform;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class PlatformTreeItem extends TreeItem<String> {

    private final Image platform = new Image(getClass().getResourceAsStream("tree/platform.png"), 25, 25, true, true);

    private final Project project;
    private JavaPlatform form;

    public PlatformTreeItem(Project pro) {
        super("JDK");
        setGraphic(new ImageView(platform));
        project = pro;
        JavaPlatform.currentPlatformProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                form = newer;
                construct();
            }
        });
        if (JavaPlatform.getCurrentPlatform() != null) {
            form = JavaPlatform.getCurrentPlatform();
            construct();
        }
    }

    private void construct() {
        getChildren().clear();
        for (JavaLibrary f : form.getAllLibs()) {
            getChildren().add(new LibraryTreeItem(project, f));
        }
    }

    public Project getProject() {
        return project;
    }

}
