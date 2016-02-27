/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.nio.file.Path;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class DirectoryTreeItem extends ProjectTreeItem {

    private static final Image parcel = new Image(LibraryTreeItem.class.getResourceAsStream("tree/parcel.PNG"), 40, 40, true, true);
    private final Path path;

    public DirectoryTreeItem(Project pro, Path dir) {
        super(pro, false);
        setGraphic(new ImageView(parcel));
        path = dir;
        setValue(dir.getFileName().toString());
    }

    public Path getPath() {
        return path;
    }

    public boolean equals(Object obj) {
        if (obj instanceof DirectoryTreeItem) {
            DirectoryTreeItem dti = (DirectoryTreeItem) obj;
            if (dti.getValue().equals(getValue())) {
                if (dti.getProject().equals(getProject())) {
                    if (dti.getPath().equals(getPath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
