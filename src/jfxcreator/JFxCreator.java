/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator;

import de.codecentric.centerdevice.MenuToolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jfxcreator.core.ProjectTree;
import jfxcreator.view.Dependencies;
import jfxcreator.view.Writer;

/**
 *
 * @author Aniket
 */
public class JFxCreator extends Application {

    public static final String OS = System.getProperty("os.name").toLowerCase();
    public static final String stylesheet = JFxCreator.class.getResource(OS.contains("win")
            ? (OS.contains("7") ? "win7.css" : "JMetroLightTheme.css") : "mac_os.css").toExternalForm();
    public static final Image icon = new Image(JFxCreator.class.getResourceAsStream("icon.png"));
    public static HostServices host;

    @Override
    public void start(Stage env) {
        host = getHostServices();
        Writer script;
        if (OS.contains("win")&&OS.contains("10")){
            
        }
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
        }
        env.getScene().getStylesheets().add(stylesheet);
        env.getIcons().add(icon);
        env.showingProperty().addListener((ob, older, newer) -> {
            if (newer) {
                script.setCurrentProject();
            }
        });
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
            E.consume();
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
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
        /*
         String compress= "/Volumes/LEXAR/Library/CEF/cef-win64.jar";
         String folderPath = "/Volumes/LEXAR/Library/CEF/";
         int BUFFER = 2048;
         System.out.println(System.currentTimeMillis());
         try{
         String uncompress;
         BufferedOutputStream dest = null;
         FileInputStream fis = new FileInputStream(compress);
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
         ZipEntry entry;
         while((entry= zis.getNextEntry())!=null) {
         uncompress = folderPath + entry.getName();
         System.out.println("Extracting entry");
         if (entry.isDirectory()) {
         File f = new File(uncompress);
         f.mkdir();
         } else {
         int count;
         byte[] data =new byte[BUFFER];
                
         FileOutputStream fos = new FileOutputStream(new File(uncompress));
         dest = new BufferedOutputStream(fos, BUFFER);
         while((count=zis.read(data, 0, BUFFER))!=-1) {
         dest.write(data, 0, count);
         }
         dest.flush();
         dest.close();
         }
                
         }
         zis.close();
         }catch(Exception e) {
         e.printStackTrace();
         }
         System.out.println(System.currentTimeMillis());
         */
//        System.exit(0);
        launch(args);
    }

}
