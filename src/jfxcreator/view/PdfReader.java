/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import jfxcreator.core.Program;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class PdfReader extends EnvironmentTab {

    public PdfReader(Program pro, Project proj) {
        super(pro, proj);
        BorderPane main = new BorderPane();
        ScrollPane pane = new ScrollPane();
        setContent(main);
        main.setCenter(pane);

//        tabPaneProperty().addListener((ob, older, newer) -> {
//            if (newer != null) {
//                addListeners();
//            }
//        });
        

    }

    private void addListeners() {
        getTabPane().widthProperty().addListener((ob, older, newer) -> {
//            op.getRoot().setMinWidth(newer.doubleValue());
        });
        getTabPane().heightProperty().addListener((ob, older, newer) -> {
//            op.getRoot().setMinHeight(newer.doubleValue()-50);
        });
    }
}
