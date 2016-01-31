/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jfxcreator.core.DebuggerController;

/**
 *
 * @author Aniket
 */
public class DebuggerConsole extends BorderPane {

    private final DebuggerController controller;
    private final VBox box;
    private final Button run, close;
    private final Label status;
    private final Label title;

    public DebuggerConsole(Pane parent, DebuggerController dc) {
        setTop(title = new Label("Debugger"));
        BorderPane.setAlignment(title, Pos.CENTER);
        setPadding(new Insets(5, 10, 5, 10));
        box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(box, Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        controller = dc;
        box.getChildren().addAll(status = new Label("Debugger is Initializing"),
                run = new Button("Launch Application"),
                close = new Button("Exit Debugger"));
        status.setStyle("-fx-text-fill:red;");
        setCenter(box);
        close.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("exit");
            }
            parent.getChildren().remove(this);
        });
        run.setOnAction((e) -> {
            dc.process("run");
        });
        run.setDisable(true);
        close.setDisable(true);
        controller.outputProperty().addListener((ob, older, newer) -> {
            Platform.runLater(() -> {
                if (newer != null) {
                    status.setText("Debugger is Running");
                    run.setDisable(false);
                    close.setDisable(false);
                } else {
                    status.setText("Debugger has terminated");
                    run.setDisable(true);
                }
            });
        });
    }
}
