/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Aniket
 */
public class EnvironmentToolBar extends ToolBar {

    private final Button newF, newP, openP;
    private final Button saveAll, undo, redo;
    private final Button build, clean, run;

    public EnvironmentToolBar(Writer sw) {
        getItems().addAll(newF = new Button("New File"),
                newP = new Button("New Project"),
                openP = new Button("Open Project"),
                saveAll = new Button("Save All"),
                new Separator(),
                undo = new Button("Undo"),
                redo = new Button("Redo"),
                new Separator(),
                build = new Button("Build"),
                clean = new Button("Clean"),
                run = new Button("Run"));
        getItems().stream().filter((n) -> (n instanceof Button)).map((n) -> (Button) n).forEach((b) -> {
            b.setText("");
        });
        newF.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/newFile.png"), 25, 25, true, true)));
        newP.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/newProjec.png"), 25, 25, true, true)));
        openP.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/openProject.png"), 25, 25, true, true)));
        saveAll.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/saveAll.png"), 25, 25, true, true)));
        undo.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/undo.png"), 25, 25, true, true)));
        redo.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/redo.png"), 25, 25, true, true)));
        build.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/build.png"), 25, 25, true, true)));
        clean.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/clean.png"), 25, 25, true, true)));
        run.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("toolbar/run.png"), 25, 25, true, true)));

        Tooltip a = new Tooltip("New File");
        Tooltip b = new Tooltip("New Project");
        Tooltip c = new Tooltip("Open Project");
        Tooltip d = new Tooltip("Save All");
        Tooltip ea = new Tooltip("Undo");
        Tooltip f = new Tooltip("Redo");
        Tooltip g = new Tooltip("Build");
        Tooltip h = new Tooltip("Clean and Build");
        Tooltip i = new Tooltip("Run Project");
        Tooltip.install(newF, a);
        Tooltip.install(newP, b);
        Tooltip.install(openP, c);
        Tooltip.install(saveAll, d);
        Tooltip.install(undo, ea);
        Tooltip.install(redo, f);
        Tooltip.install(build, g);
        Tooltip.install(clean, h);
        Tooltip.install(run, i);

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
    }

}
