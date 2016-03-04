/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import tachyon.core.DebuggerController;

/**
 *
 * @author Aniket
 */
public class DebuggerConsole extends BorderPane {

    private final DebuggerController controller;
    private final VBox box;
    private final Button run, close, threads, suspend, resume, list, brea,
            classpath, monitor;
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
                run = new Button("Run"),
                brea = new Button("Set Breakpoints"),
                threads = new Button("Threads"),
                suspend = new Button("Suspend"),
                resume = new Button("Resume"),
                list = new Button("List"),
                classpath = new Button("Classpath"),
                monitor = new Button("Monitor"),
                close = new Button("Exit Debugger"));
        for (Node n : box.getChildren()) {
            n.setStyle("-fx-font-size:12");
        }
        status.setStyle("-fx-text-fill:red;");
        setCenter(box);
        close.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("exit");
            }
            parent.getChildren().remove(this);
        });
        run.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("run");
            }
        });
        threads.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("threads");
            }
        });
        suspend.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("suspend");
            }
        });
        resume.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("resume");
            }
        });
        list.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("list");
            }
        });
        classpath.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("classpath");
            }
        });
        monitor.setOnAction((e) -> {
            if (dc.isAvailable()) {
                dc.process("monitor");
            }
        });
        brea.setOnAction((E) -> {
            if (dc.isAvailable()) {
                dc.setBreakpoints();
            }
        });
        run.setDisable(true);
        threads.setDisable(true);
        suspend.setDisable(true);
        resume.setDisable(true);
        list.setDisable(true);
        classpath.setDisable(true);
        monitor.setDisable(true);
        close.setDisable(true);
        brea.setDisable(true);
        controller.outputProperty().addListener((ob, older, newer) -> {
            Platform.runLater(() -> {
                if (newer != null) {
                    status.setText("Debugger is Running");
                    run.setDisable(false);
                    close.setDisable(false);
                    brea.setDisable(false);
                    threads.setDisable(false);
                    suspend.setDisable(false);
                    resume.setDisable(false);
                    list.setDisable(false);
                    classpath.setDisable(false);
                    monitor.setDisable(false);
                } else {
                    status.setText("Debugger has terminated");
                    run.setDisable(true);
                    threads.setDisable(true);
                    brea.setDisable(true);
                    suspend.setDisable(true);
                    resume.setDisable(true);
                    list.setDisable(true);
                    classpath.setDisable(true);
                    monitor.setDisable(true);
                }
            });
        });
    }
}
