/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.nio.file.Path;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class DirectoryTreeItem extends ProjectTreeItem {

    private final Path path;

    public DirectoryTreeItem(Project pro, Path dir) {
        super(pro);
        path = dir;
        setValue(dir.getFileName().toString());
        getChildren().addListener((ListChangeListener.Change<? extends TreeItem<String>> c) -> {
            c.next();
            if (getChildren().size() == 0) {
                if (getParent() != null) {
                    getParent().getChildren().remove(DirectoryTreeItem.this);
                }
            }
        });
    }

    public Path getPath() {
        return path;
    }

}
