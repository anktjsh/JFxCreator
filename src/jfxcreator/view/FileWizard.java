/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import static jfxcreator.JFxCreator.icon;
import jfxcreator.core.Project;

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
        box.getChildren().add(new Label("Choose your file type"));
        box.setAlignment(Pos.CENTER_LEFT);
        stage.setScene(new Scene(box));
        options = new ListView<>();
        options.setItems(FXCollections.observableArrayList("Java Class",
                "Java Main Class",
                "Java Interface",
                /*"JavaFx Main Class",
                 "JavaFx Preloader",
                 */
                "Empty Java File",
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
                    Alert al = new Alert(Alert.AlertType.WARNING);
                    al.initOwner(stage);
                    al.setHeaderText("You must select a file type first");
                    ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
                    al.showAndWait();
                }
            } else {
                if (destination.getText().length() > 0) {
                    finish();
                } else {
                    Alert al = new Alert(Alert.AlertType.ERROR);
                    al.setTitle("Error");
                    al.initOwner(stage);
                    al.setHeaderText("File");
                    ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
                    al.setContentText("File name cannot be empty!");
                    al.showAndWait();
                }
            }
        });
        cancel.setOnAction((e) -> {
            cancel();
        });
    }

    private void switchtoComplete() {
        box.getChildren().remove(options);
        box.getChildren().add(1, getNode((options.getSelectionModel().getSelectedItem())));
        next.setText("Finish");
    }

    private String newName;

    private TextField filename, className, projectName, destination;
    private Button back;
    private String descriptor;

    private Pane getNode(String type) {
        descriptor = type;
        VBox root = new VBox();
        root.setPadding(new Insets(5, 10, 5, 10));
        root.setSpacing(15);
        root.getChildren().addAll(projectName = new TextField(project.getProjectName()),
                filename = new TextField("NewFile"),
                destination = new TextField());
        projectName.setEditable(false);
        destination.setEditable(false);
        if (type.equals("Empty File")) {

        } else {
            filename.setText(filename.getText() + ".java");
            filename.setEditable(false);
            root.getChildren().add(1, className = new TextField("NewFile"));
            className.textProperty().addListener((ob, older, newer) -> {
                filename.setText(newer + ".java");
            });
        }
        String sourcepath = project.getSource().toAbsolutePath().toString();
        destination.setText(sourcepath + File.separator + filename.getText());
        filename.textProperty().addListener((ob, older, newer) -> {
            destination.setText(sourcepath + File.separator + newer);
        });
        bottom.getChildren().addAll(1, FXCollections.observableArrayList(back = new Button("Back")));
        back.setOnAction((e) -> {
            back(root);
        });
        return root;
    }

    private void cancel() {
        newName = null;
        stage.close();
    }

    private void finish() {
        String temp = destination.getText();
        Path f = Paths.get(temp);
        if (Files.exists(f)) {
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("New File");
            al.setHeaderText("File : " + temp);
            al.setContentText("File already exists!");
            al.initOwner(stage);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            al.showAndWait();
        } else {
            newName = className.getText();
            stage.close();
        }
    }

    private void back(Pane n) {
        switchBack(n);
    }

    private void switchBack(Pane n) {
        box.getChildren().add(1, options);
        box.getChildren().remove(n);
        bottom.getChildren().removeAll(back);
        next.setText("Next");
        stage.sizeToScene();
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
            return getTemplateCode(desc, pack, clas);
        } else {
            return getTemplateCode(desc, null, className);
        }
    }

    private static List<String> getTemplateCode(String desc, String pack, String clas) {
        switch (desc) {
            case "Java Class":
                return one(pack, clas);
            case "Java Main Class":
                return two(pack, clas);
            case "Java Interface":
                return three(pack, clas);
//            case "JavaFx Main Class":
//                return four(pack, clas);
//            case "JavaFx Preloader":
//                return five(pack, clas);
            case "Empty Java File":
                return six(pack, clas);
            case "Other File":
                return seven(pack, clas);
            default:
                return new ArrayList<>();
        }

    }

    private static List<String> one(String pack, String clas) {
        if (pack != null) {
            String list = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> two(String pack, String clas) {
        if (pack != null) {
            String list = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "    public static void main (String args[]) {\n"
                    + "        System.out.println(\"Hello, World!\");\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "    public static void main (String args[]) {\n"
                    + "        System.out.println(\"Hello, World!\");\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> three(String pack, String clas) {
        if (pack != null) {
            String list = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "public interface " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public interface " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

//    private static List<String> four(String pack, String clas) {
//        
//    }
//
//    private static List<String> five(String pack, String clas) {
//        
//    }
    private static List<String> six(String pack, String clas) {
        return new ArrayList<>();
    }

    private static List<String> seven(String pack, String clas) {
        return new ArrayList<>();
    }

}
