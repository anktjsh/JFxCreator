/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Aniket
 */
public class ProjectsView extends BorderPane {

    private final TreeView<String> tree;
    private final Button collapse;
    private final VBox box;

    public ProjectsView() {
        collapse = new Button("Projects");
        collapse.setRotate(270);
        setLeft(box = new VBox(new Label(), new Label(), new Label(), collapse));
        box.setStyle("-fx-background-color:white;");
        box.setAlignment(Pos.TOP_CENTER);
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
