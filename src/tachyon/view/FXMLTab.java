/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import tachyon.framework.core.Project;
import tachyon.java.core.Resource;

/**
 *
 * @author Aniket
 */
public class FXMLTab extends Editor {

    private final Button open;

    public FXMLTab(Resource sc, Project pro) {
        super(sc, pro);
        TabToolbar top = (TabToolbar) getCenter().getTop();
        top.getItems().addAll(new Separator(),
                open = new Button("Open in Scene Builder"));
        open.setOnAction((e) -> {
            File f = new File("SceneBuilder-8.1.1.jar");
            if (!f.exists()) {
                try {
                    Files.copy(getClass().getResourceAsStream("scenebuilder/SceneBuilder-8.1.1.jar"), f.toPath());
                } catch (IOException ex) {
                }
            }
            if (f.exists()) {
                (new Thread(() -> {
                    launchSceneBuilder();
                })).start();
            }
        });
    }

    public final void launchSceneBuilder() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            launchWindowsSceneBuilder();
        } else {
            launchMacSceneBuilder();
        }
        getScript().reload();
        Platform.runLater(() -> {
            reload();
        });
    }

    private void launchWindowsSceneBuilder() {
        String JAVA_HOME = Dependencies.localVersionProperty.get();
        String one = "\"" + JAVA_HOME + File.separator + "java\"" + " -jar SceneBuilder-8.1.1.jar " + getScript().getFile().toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        try {
            Process start = pb.start();
            (new Thread(new Project.OutputReader(start.getInputStream(), null))).start();
            (new Thread(new Project.ErrorReader(start.getErrorStream(), null))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    private void launchMacSceneBuilder() {
        String JAVA_HOME = Dependencies.localVersionProperty.get();
        String one = JAVA_HOME + File.separator + "java" + " -jar SceneBuilder-8.1.1.jar " + getScript().getFile().toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        try {
            Process start = pb.start();
            (new Thread(new Project.OutputReader(start.getInputStream(), null))).start();
            (new Thread(new Project.ErrorReader(start.getErrorStream(), null))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

}
