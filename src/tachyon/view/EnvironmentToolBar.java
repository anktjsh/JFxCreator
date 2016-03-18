/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tachyon.memory.Monitor;

/**
 *
 * @author Aniket
 */
public class EnvironmentToolBar extends ToolBar {

    private final Button newF, newP, openP, error;
    private final Button saveAll, undo, redo;
    private final Button cut, copy, debug, paste;
    private final Button build, clean, run, monitor;

    public EnvironmentToolBar(Writer sw) {
        getItems().addAll(newF = new Button("New File"),
                newP = new Button("New Project"),
                openP = new Button("Open Project"),
                saveAll = new Button("Save All"),
                new Separator(),
                undo = new Button("Undo"),
                redo = new Button("Redo"),
                cut = new Button("Cut"),
                copy = new Button("Copy"),
                paste = new Button("Paste"),
                new Separator(),
                build = new Button("Build"),
                clean = new Button("Clean"),
                run = new Button("Run"),
                new Separator(),
                debug = new Button("Debug"),
                new Separator(),
                monitor = new Button("Monitor"), 
                new Separator(), 
                error = new Button("Error"));
        for (javafx.scene.Node n : getItems()){
            if (n instanceof Button) {
                Button b = (Button)n;
                b.setText("");
            }
        }
        newF.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/newFile.png"), 25, 25, true, true)));
        newP.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/newProjec.png"), 25, 25, true, true)));
        openP.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/openProject.png"), 25, 25, true, true)));
        saveAll.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/saveAll.png"), 25, 25, true, true)));
        undo.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/undo.png"), 25, 25, true, true)));
        redo.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/redo.png"), 25, 25, true, true)));
        build.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/build.png"), 25, 25, true, true)));
        clean.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/clean.png"), 25, 25, true, true)));
        run.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/run.png"), 25, 25, true, true)));
        cut.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/cut.png"), 25, 25, true, true)));
        copy.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/copy.png"), 25, 25, true, true)));
        paste.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/paste.png"), 25, 25, true, true)));
        debug.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/debug.png"), 25, 25, true, true)));
        monitor.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/monitor.png"), 25, 25, true, true)));
        error.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/error.png"), 25, 25, true, true)));

        Tooltip a = new Tooltip("New File");
        Tooltip b = new Tooltip("New Project");
        Tooltip c = new Tooltip("Open Project");
        Tooltip d = new Tooltip("Save All");
        Tooltip ea = new Tooltip("Undo");
        Tooltip f = new Tooltip("Redo");
        Tooltip g = new Tooltip("Build");
        Tooltip h = new Tooltip("Clean and Build");
        Tooltip i = new Tooltip("Run Project");
        Tooltip j = new Tooltip("Cut");
        Tooltip k = new Tooltip("Copy");
        Tooltip l = new Tooltip("Paste");
        Tooltip m = new Tooltip("Debug");
        Tooltip n = new Tooltip("Memory Monitor");
        Tooltip o = new Tooltip("Error Console");
        Tooltip.install(newF, a);
        Tooltip.install(newP, b);
        Tooltip.install(openP, c);
        Tooltip.install(saveAll, d);
        Tooltip.install(undo, ea);
        Tooltip.install(redo, f);
        Tooltip.install(build, g);
        Tooltip.install(clean, h);
        Tooltip.install(run, i);
        Tooltip.install(cut, j);
        Tooltip.install(copy, k);
        Tooltip.install(paste, l);
        Tooltip.install(debug, m);
        Tooltip.install(monitor, n);
        Tooltip.install(error, o);

        newF.setOnAction((e) -> {
            sw.newFile();
        });
        newP.setOnAction((e) -> {
            sw.newProject();
        });
        openP.setOnAction((e) -> {
            sw.openProject();
        });
        saveAll.setOnAction((e) -> {
            sw.saveAll();
        });
        undo.setOnAction((e) -> {
            sw.undo();
        });
        redo.setOnAction((e) -> {
            sw.redo();
        });
        build.setOnAction((e) -> {
            sw.build();
        });
        clean.setOnAction((e) -> {
            sw.clean();
        });
        run.setOnAction((e) -> {
            sw.run();
        });
        cut.setOnAction((e) -> {
            sw.cut();
        });
        copy.setOnAction((e) -> {
            sw.copy();
        });
        paste.setOnAction((e) -> {
            sw.paste();
        });
        debug.setOnAction((e) -> {
            sw.debug();
        });
        monitor.setOnAction((e) -> {
            Monitor.show();
        });
        error.setDisable(true);
        ErrorConsole.getErrors().addListener((ListChangeListener.Change<? extends ErrorConsole.Error> c1) -> {
            c1.next();
            if (c1.getList().isEmpty()) {
                error.setDisable(true);
            } else {
                error.setDisable(false);
            }
        });
        error.setOnAction((e) -> {
            ErrorConsole.show();
        });
    }

}
