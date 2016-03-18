/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Aniket
 */
public class ProjectsView extends BorderPane {

    private final TreeView<String> tree;
    private final Button collapse;
    private final ToolBar box;

    public ProjectsView() {
        collapse = new Button("Projects");
        collapse.setRotate(270);
        collapse.setGraphic(new ImageView(ProjectTreeItem.proj));
        setLeft(box = new ToolBar(new Label(), new Label(), collapse));
        box.setOrientation(Orientation.VERTICAL);
        setCenter(null);
        tree = new TreeView<>();
        collapse.setOnAction((e) -> {
            if (getCenter() == null) {
                setCenter(tree);
            } else {
                setCenter(null);
            }
        });
        BorderPane.setAlignment(tree, Pos.TOP_CENTER);
    }

    public TreeView<String> getTreeView() {
        return tree;
    }
}
