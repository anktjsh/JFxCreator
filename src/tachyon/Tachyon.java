/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon;

import de.codecentric.centerdevice.MenuToolkit;
import java.io.File;
import java.util.Optional;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tachyon.core.ProjectTree;
import tachyon.memory.Monitor;
import tachyon.view.Dependencies;
import tachyon.view.Details;
import tachyon.view.Writer;

/**
 *
 * @author Aniket
 */
public class Tachyon extends Application {

    public static final String OS = System.getProperty("os.name").toLowerCase();
    public static final Image icon = new Image(Tachyon.class.getResourceAsStream("icon.png"));
    public static HostServices host;
    public static String css = Tachyon.class.getResource("material.css").toExternalForm();
    public static BooleanProperty applyCss = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage env) {
        host = getHostServices();
        Monitor.initialize(env);
        Writer script;
        env.setScene(new Scene(script = new Writer()));
        env.getScene().getStylesheets().add(getClass().getResource("java-keywords.css").toExternalForm());
        if (applyCss.get()) {
            env.getScene().getStylesheets().add(css);
        }
        applyCss.addListener((ob, older, newer) -> {
            if (newer) {
                env.getScene().getStylesheets().add(css);
            } else {
                env.getScene().getStylesheets().remove(css);
            }
        });
        env.setOnCloseRequest((e) -> {
            close(script, e);
        });
        env.setTitle("Tachyon");
        Writer.currentProject.addListener((ob, older, newer) -> {
            if (newer != null) {
                env.setTitle("Tachyon - " + newer.getProjectName());
            } else {
                env.setTitle("Tachyon");
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
            Menu def = tk.createDefaultApplicationMenu("Tachyon");
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
            Alert al = new Alert(AlertType.CONFIRMATION);
            al.initOwner(script.getScene().getWindow());
            al.setHeaderText("Would you like to save before closing?");
            al.getButtonTypes().clear();
            al.getButtonTypes().addAll(ButtonType.OK, ButtonType.NO, ButtonType.CANCEL);
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
        try {
            File f = new File(".cache" + File.separator + "design03.txt");
            Scanner in = new Scanner(f);
            applyCss.set(Boolean.parseBoolean(in.nextLine()));
        } catch (Exception e) {
        }
        launch(args);
    }

}
