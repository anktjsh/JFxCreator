/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.geometry.Pos;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Aniket
 */
public class ProjectsView extends BorderPane {

    private final TreeView<String> tree;

    public ProjectsView() {
        setCenter(tree = new TreeView<>());
        BorderPane.setAlignment(tree, Pos.TOP_CENTER);
    }

    public TreeView<String> getTreeView() {
        return tree;
    }
}
