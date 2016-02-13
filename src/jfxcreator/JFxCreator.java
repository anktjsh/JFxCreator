/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator;

import de.codecentric.centerdevice.MenuToolkit;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jfxcreator.core.ProjectTree;
import jfxcreator.memory.Monitor;
import jfxcreator.view.Dependencies;
import jfxcreator.view.Writer;

/**
 *
 * @author Aniket
 */
public class JFxCreator extends Application {

    public static final String OS = System.getProperty("os.name").toLowerCase();
    public static final Image icon = new Image(JFxCreator.class.getResourceAsStream("icon.png"));
    public static HostServices host;

    @Override
    public void start(Stage env) {
        host = getHostServices();
        Monitor.initialize(env);
        Writer script;
        env.setScene(new Scene(script = new Writer()));
        env.getScene().getStylesheets().add(getClass().getResource("java-keywords.css").toExternalForm());
        env.setOnCloseRequest((e) -> {
            close(script, e);
        });
        env.setTitle("JFxCreator");
        Writer.currentProject.addListener((ob, older, newer) -> {
            if (newer != null) {
                env.setTitle("JFxCreator - " + newer.getProjectName());
            } else {
                env.setTitle("JFxCreator");
            }
        });
        env.setMaximized(true);
        if (OS.contains("mac")) {
            env.setFullScreen(true);
            com.sun.glass.ui.Application.GetApplication().setEventHandler(new com.sun.glass.ui.Application.EventHandler() {

                @Override
                public void handleQuitAction(com.sun.glass.ui.Application app, long time) {
                    super.handleQuitAction(app, time);
                    close(script, null);
                }

                @Override
                public void handleOpenFilesAction(com.sun.glass.ui.Application app, long time, String[] files) {
                    super.handleOpenFilesAction(app, time, files); //To change body of generated methods, choose Tools | Templates.
                    script.loadFiles(files);
                }

            });
        }
        env.getIcons().add(icon);
        env.show();
        setMenuBar(script);
        Dependencies.load(env);
    }

    private void setMenuBar(Writer script) {
        if (OS.contains("mac")) {
            MenuToolkit tk = MenuToolkit.toolkit();
            Menu def = tk.createDefaultApplicationMenu("JFxCreator");
            tk.setApplicationMenu(def);
            def.getItems().get(3).setOnAction((E) -> {
                close(script, E);
            });
        }
    }

    private void close(Writer script, Event E) {
        if (!script.processCheck()) {
            if (E != null) {
                E.consume();
            }
            return;
        }
        if (script.canSave()) {
            Alert al = new Alert(Alert.AlertType.CONFIRMATION);
            al.setHeaderText("Would you like to save before closing?");
            al.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.NO);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            al.initOwner(script.getScene().getWindow());
            Optional<ButtonType> show = al.showAndWait();
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    script.saveAll();
                } else if (show.get() == ButtonType.CANCEL) {
                    E.consume();
                    return;
                }
            }
        }
        script.saveOpenProjectsInformation();
        ProjectTree.getTree().close();
        Platform.exit();
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
