/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import tachyon.core.ProcessItem;

/**
 *
 * @author Aniket
 */
public class ConsolePane extends BorderPane {

    private final TabPane console;
    private final Button out;

    public ConsolePane() {
        console = new TabPane();
        setPrefHeight(0);
        setCenter(console);
        BorderPane.setMargin(console, new Insets(5));
        setBottom(new ToolBar(out = new Button("Output")));
        out.setOnAction((e) -> {
            if (getPrefHeight() == 0) {
                setPrefHeight(300);
            } else {
                setPrefHeight(0);
            }
        });
        console.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) -> {
            c.next();
            if (c.wasAdded()) {
                setPrefHeight(300);
            }
        });
    }

    public void addConsoleWindow(ProcessItem c) {
        ConsoleWindow con;
        console.getTabs().add(con = new ConsoleWindow(c));
        console.getSelectionModel().select(con);
    }

}
