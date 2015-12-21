/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import static jfxcreator.JFxCreator.stylesheet;
import jfxcreator.core.Program;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class ProjectProperties {

    private final Stage stage;
    private final VBox box;
    private final TextField mainClass;
    private final Button select, confirm, cancel;
    private final Project project;

    private final ListView<String> libsView;
    private final Button addJar, removeJar;

    public ProjectProperties(Project project, Window w) {
        this.project = project;
        stage = new Stage();
        stage.initOwner(w);
        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.setWidth(400);
//        stage.setHeight(400);
        stage.setTitle("Project Properties");
        stage.getIcons().add(jfxcreator.JFxCreator.icon);
        stage.setResizable(false);
        HBox mai, libs, one;
        stage.setScene(new Scene(box = new VBox(15,
                one = new HBox(10, new Label("Main-Class"),
                        mainClass = new TextField(project.getMainClassName()),
                        select = new Button("Select")),
                libs = new HBox(10, new Label("External Libraries"),
                        libsView = new ListView<>(),
                        new VBox(5,
                                addJar = new Button("Add Jar"),
                                removeJar = new Button("Remove Jar"))),
                mai = new HBox(10,
                        cancel = new Button("Cancel"),
                        confirm = new Button("Confirm")))));
        stage.getScene().getStylesheets().add(stylesheet);
        box.setPadding(new Insets(5, 10, 5, 10));
        mai.setAlignment(Pos.CENTER_RIGHT);
        libs.setAlignment(Pos.CENTER);
        one.setAlignment(Pos.CENTER);
        box.setAlignment(Pos.CENTER);
        mainClass.setEditable(false);

        for (String s : project.getAllLibs()) {
            libsView.getItems().add(s);
        }

        addJar.setOnAction((e) -> {
            FileChooser f = new FileChooser();
            f.setTitle("External Libraries");
            f.setSelectedExtensionFilter(new ExtensionFilter("Jar Files", "*.jar"));
            List<File> showOpenMultipleDialog = f.showOpenMultipleDialog(stage);
            if (showOpenMultipleDialog != null) {
                for (File fi : showOpenMultipleDialog) {
                    if (!libsView.getItems().contains(fi.getAbsolutePath())) {
                        libsView.getItems().add(fi.getAbsolutePath());
                    }
                }
            }
        });

        removeJar.setOnAction((e) -> {
            String select = libsView.getSelectionModel().getSelectedItem();
            if (select != null) {
                libsView.getItems().remove(select);
            }
        });

        select.setOnAction((e) -> {
            List<String> all = getAll();
            ChoiceDialog<String> di = new ChoiceDialog<>(project.getMainClassName(), all);
            di.setTitle("Select Main Class");
            di.initOwner(stage);
            di.setHeaderText("Select A Main Class");
            Optional<String> show = di.showAndWait();
            if (show.isPresent()) {
                mainClass.setText(show.get());
            }
        });
        cancel.setOnAction((e) -> {
            stage.close();
        });
        confirm.setOnAction((e) -> {
            project.setMainClassName(mainClass.getText());
            project.setAllLibs(libsView.getItems());
            stage.close();
        });
    }

    private List<String> getAll() {
        ArrayList<String> al = new ArrayList<>();
        for (Program pr : project.getPrograms()) {
            List<String> str = pr.getLastCode();
            ArrayList<String> ret = new ArrayList<>();
            for (String s : str) {
                if (s.contains("public") && s.contains("static") && s.contains("void") && s.contains("main")) {
                    ret.add(s);
                }
            }
            if (!ret.isEmpty()) {
                al.add(pr.getClassName());
            }
        }

        return al;
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}
