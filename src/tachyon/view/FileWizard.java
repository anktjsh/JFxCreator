/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;
import static tachyon.Tachyon.icon;
import tachyon.core.Project;
import tachyon.features.Template;

/**
 *
 * @author Aniket
 */
public class FileWizard {

    private final Stage stage;
    private final Project project;
    private final ListView<String> options;
    private final VBox box;
    private final Button next, cancel;
    private final HBox bottom;
    private final Label label;

    public FileWizard(Window w, Project pro) {
        stage = new Stage();
        stage.setTitle("New File");
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        stage.setResizable(false);
        stage.initOwner(w);
        stage.getIcons().add(icon);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnCloseRequest((e) -> {
            e.consume();
            cancel();
        });
        project = pro;
        box = new VBox(10);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.getChildren().add(label = new Label("Choose your file type"));
        box.setAlignment(Pos.CENTER_LEFT);
        stage.setScene(new Scene(box));
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        options = new ListView<>();
        options.setItems(FXCollections.observableArrayList("Java Class",
                "Java Main Class",
                "Java Interface",
                "JavaFx Main Class",
                "JavaFx Preloader",
                "Empty Java File",
                "Empty FXML File",
                "Empty Text File",
                "Empty HTML File",
                "Other File"));
        options.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                switchtoComplete();
            }
        });
        box.getChildren().add(options);
        box.getChildren().add(bottom = new HBox(5));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.getChildren().addAll(cancel = new Button("Cancel"),
                next = new Button("Next"));
        next.setOnAction((e) -> {
            if (next.getText().equals("Next")) {
                if (options.getSelectionModel().getSelectedItem() != null) {
                    switchtoComplete();
                } else {
                    Writer.showAlert(Alert.AlertType.WARNING, stage, "", "You must select a file type first", "");
                }
            } else if (destination.getText().length() > 0) {
                finish();
            } else {
                Writer.showAlert(Alert.AlertType.ERROR, stage, "Error", "File", "File name cannot be empty");
            }
        });
        cancel.setOnAction((e) -> {
            cancel();
        });
    }

    private void switchtoComplete() {
        if (options.getSelectionModel().getSelectedItem() != null) {
            box.getChildren().remove(options);
            label.setText("Enter a filename");
            box.getChildren().add(1, getNode((options.getSelectionModel().getSelectedItem())));
            next.setText("Finish");
        }
    }

    private String newName;

    private TextField filename, projectName, destination, packageName;
    private Button back;
    private String descriptor;

    private Pane getNode(String type) {
        descriptor = type;
        VBox root = new VBox();
        root.setPadding(new Insets(5, 10, 5, 10));
        root.setSpacing(15);
        root.getChildren().addAll(
                projectName = new TextField(project.getProjectName()),
                new Label("Package Name"),
                packageName = new TextField(""),
                new Label("File Name"),
                filename = new TextField("NewFile"),
                destination = new TextField());
        projectName.setEditable(false);
        projectName.setPromptText("Project Name");
        destination.setEditable(false);
        packageName.setPromptText("Package Name");
        filename.setPromptText("FileName");
        destination.setPromptText("File Path");

        String sourcepath = project.getSource().toAbsolutePath().toString();
        destination.setText(sourcepath + File.separator + filename.getText());

        bottom.getChildren().addAll(1, FXCollections.observableArrayList(back = new Button("Back")));
        back.setOnAction((e) -> {
            back(root);
        });
        if (type.contains("Java")) {
            filename.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(packageName.getText()) + File.separator + newer + ".java");
            });
            destination.setText(destination.getText() + ".java");
            packageName.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(newer) + File.separator + filename.getText() + ".java");
            });
        } else if (type.contains("FXML")
                || type.contains("HTML")
                || type.contains("Text")) {
            String extension;
            if (type.contains("FXML")) {
                extension = ".fxml";
            } else if (type.contains("HTML")) {
                extension = ".html";
            } else {
                extension = ".txt";
            }
            filename.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(packageName.getText()) + File.separator + newer + extension);
            });
            destination.setText(destination.getText() + extension);
            packageName.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(newer) + File.separator + filename.getText() + extension);
            });
        } else {
            filename.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(packageName.getText()) + File.separator + newer);
            });
            packageName.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(newer) + File.separator + filename.getText());
            });
        }

        return root;
    }

    private String getDir(String dots) {
        return replaceAll(dots, ".", File.separator);
    }

    private String replaceAll(String txt, String init, String repl) {
        while (txt.contains(init)) {
            int index = txt.indexOf(init);
            String first = txt.substring(0, index);
            String two = txt.substring(index + 1);
            txt = first + repl + two;
        }
        return txt;
    }

    private void cancel() {
        newName = null;
        stage.close();
    }

    private void finish() {
        String temp = destination.getText();
        Path f = Paths.get(temp);
        if (Files.exists(f)) {
            Writer.showAlert(Alert.AlertType.ERROR, stage, "New File", "File : " + temp, "File already exists!");
        } else {
            newName = packageName.getText().isEmpty() ? "" : (packageName.getText() + ".") + filename.getText();
            stage.close();
        }
    }

    private void back(Pane n) {
        switchBack(n);
    }

    private void switchBack(Pane n) {
        box.getChildren().remove(1);
        label.setText("Choose your file type");
        box.getChildren().add(1, options);
        bottom.getChildren().removeAll(back);
        next.setText("Next");
    }

    public Optional<FileDescription> showAndWait() {
        stage.showAndWait();
        if (newName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(new FileDescription(newName, descriptor));
    }

    public class FileDescription {

        private final String name, description;

        public FileDescription(String a, String b) {
            name = a;
            description = b;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    public static List<String> getTemplateCode(String desc, String className) {
        if (className.contains(".")) {
            String pack = className.substring(0, className.lastIndexOf('.'));
            String clas = className.substring(className.lastIndexOf('.') + 1);
            return Template.getTemplateCode(desc, pack, clas);
        } else {
            return Template.getTemplateCode(desc, null, className);
        }
    }

}
