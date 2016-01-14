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
import static jfxcreator.JFxCreator.stylesheet;
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
        stage.getScene().getStylesheets().add(stylesheet);
        options = new ListView<>();
        options.setItems(FXCollections.observableArrayList("Java Class",
                "Java Main Class",
                "Java Interface",
                "JavaFx Main Class",
                "JavaFx Preloader",
                "Empty Java File",
                "Empty FXML File",
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
        } else if (type.contains("FXML")) {
            filename.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(packageName.getText()) + File.separator + newer + ".fxml");
            });
            destination.setText(destination.getText() + ".fxml");
            packageName.textProperty().addListener((ob, older, newer) -> {
                destination.setText(sourcepath + File.separator + getDir(newer) + File.separator + filename.getText() + ".fxml");
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
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("New File");
            al.setHeaderText("File : " + temp);
            al.setContentText("File already exists!");
            al.initOwner(stage);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            al.showAndWait();
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
            case "JavaFx Main Class":
                return four(pack, clas);
            case "JavaFx Preloader":
                return five(pack, clas);
            case "Empty Java File":
                return six(pack, clas);
            case "Other File":
                return seven(pack, clas);
            case "Empty FXML File":
                return eight();
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

    private static List<String> four(String pack, String clas) {
        if (pack != null) {
            String list
                    = "package " + pack + ";\n"
                    + "\n"
                    + "import javafx.application.Application;\n"
                    + "import javafx.event.ActionEvent;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.Button;\n"
                    + "import javafx.scene.layout.StackPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Application {\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage primaryStage) {\n"
                    + "        Button btn = new Button();\n"
                    + "        btn.setText(\"Say 'Hello World'\");\n"
                    + "        btn.setOnAction((ActionEvent event) -> {\n"
                    + "            System.out.println(\"Hello World!\");\n"
                    + "        });\n"
                    + "        \n"
                    + "        StackPane root = new StackPane();\n"
                    + "        root.getChildren().add(btn);\n"
                    + "        \n"
                    + "        Scene scene = new Scene(root, 300, 250);\n"
                    + "        \n"
                    + "        primaryStage.setTitle(\"Hello World!\");\n"
                    + "        primaryStage.setScene(scene);\n"
                    + "        primaryStage.show();\n"
                    + "    }\n"
                    + "\n"
                    + "    public static void main(String[] args) {\n"
                    + "        launch(args);\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list
                    = "\n"
                    + "import javafx.application.Application;\n"
                    + "import javafx.event.ActionEvent;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.Button;\n"
                    + "import javafx.scene.layout.StackPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Application {\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage primaryStage) {\n"
                    + "        Button btn = new Button();\n"
                    + "        btn.setText(\"Say 'Hello World'\");\n"
                    + "        btn.setOnAction((ActionEvent event) -> {\n"
                    + "            System.out.println(\"Hello World!\");\n"
                    + "        });\n"
                    + "        \n"
                    + "        StackPane root = new StackPane();\n"
                    + "        root.getChildren().add(btn);\n"
                    + "        \n"
                    + "        Scene scene = new Scene(root, 300, 250);\n"
                    + "        \n"
                    + "        primaryStage.setTitle(\"Hello World!\");\n"
                    + "        primaryStage.setScene(scene);\n"
                    + "        primaryStage.show();\n"
                    + "    }\n"
                    + "\n"
                    + "    public static void main(String[] args) {\n"
                    + "        launch(args);\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }

    }

    private static List<String> five(String pack, String clas) {
        if (pack != null) {
            String list
                    = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "import javafx.application.Preloader;\n"
                    + "import javafx.application.Preloader.ProgressNotification;\n"
                    + "import javafx.application.Preloader.StateChangeNotification;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.ProgressBar;\n"
                    + "import javafx.scene.layout.BorderPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Preloader {\n"
                    + "    \n"
                    + "    ProgressBar bar;\n"
                    + "    Stage stage;\n"
                    + "    \n"
                    + "    private Scene createPreloaderScene() {\n"
                    + "        bar = new ProgressBar();\n"
                    + "        BorderPane p = new BorderPane();\n"
                    + "        p.setCenter(bar);\n"
                    + "        return new Scene(p, 300, 150);        \n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage stage) throws Exception {\n"
                    + "        this.stage = stage;\n"
                    + "        stage.setScene(createPreloaderScene());        \n"
                    + "        stage.show();\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleStateChangeNotification(StateChangeNotification scn) {\n"
                    + "        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {\n"
                    + "            stage.hide();\n"
                    + "        }\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleProgressNotification(ProgressNotification pn) {\n"
                    + "        bar.setProgress(pn.getProgress());\n"
                    + "    }    \n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list
                    = "\n"
                    + "import javafx.application.Preloader;\n"
                    + "import javafx.application.Preloader.ProgressNotification;\n"
                    + "import javafx.application.Preloader.StateChangeNotification;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.ProgressBar;\n"
                    + "import javafx.scene.layout.BorderPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Preloader {\n"
                    + "    \n"
                    + "    ProgressBar bar;\n"
                    + "    Stage stage;\n"
                    + "    \n"
                    + "    private Scene createPreloaderScene() {\n"
                    + "        bar = new ProgressBar();\n"
                    + "        BorderPane p = new BorderPane();\n"
                    + "        p.setCenter(bar);\n"
                    + "        return new Scene(p, 300, 150);        \n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage stage) throws Exception {\n"
                    + "        this.stage = stage;\n"
                    + "        stage.setScene(createPreloaderScene());        \n"
                    + "        stage.show();\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleStateChangeNotification(StateChangeNotification scn) {\n"
                    + "        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {\n"
                    + "            stage.hide();\n"
                    + "        }\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleProgressNotification(ProgressNotification pn) {\n"
                    + "        bar.setProgress(pn.getProgress());\n"
                    + "    }    \n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> six(String pack, String clas) {
        return new ArrayList<>();
    }

    private static List<String> seven(String pack, String clas) {
        return new ArrayList<>();
    }

    private static List<String> eight() {
        String list = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<?import java.lang.*?>\n"
                + "<?import java.util.*?>\n"
                + "<?import javafx.scene.*?>\n"
                + "<?import javafx.scene.control.*?>\n"
                + "<?import javafx.scene.layout.*?>\n"
                + "\n"
                + "<AnchorPane id=\"AnchorPane\" prefHeight=\"400.0\" prefWidth=\"600.0\" xmlns:fx=\"http://javafx.com/fxml/1\">\n"
                + "    \n"
                + "</AnchorPane>\n"
                + "";
        return FXCollections.observableArrayList(list.split("\n"));
    }

}
