/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;
import static tachyon.Tachyon.icon;
import tachyon.core.Highlighter;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class ProjectWizard {

    public static Project createProject(Window w, int n) {
        String loca = Dependencies.workplace_location;
        Path f = Paths.get(loca);
        if (!Files.exists(f)) {
            try {
                Files.createDirectories(f);
            } catch (IOException ex) {
            }
        }
        List<String> file = new ProjectWizard(w).showAndWait();
        if (file.isEmpty()) {
            return null;
        }
        return new Project(Paths.get(file.get(0)), file.get(1), true, n);
    }

    private final Stage stage;
    private final TextField projectPath, projectName, packageName, mainClassName;
    private final Button confirm, cancel;
    private final VBox box;

    public ProjectWizard(Window w) {
        stage = new Stage();
        stage.setTitle("New Project");
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(icon);
        stage.initOwner(w);
        stage.setResizable(false);
        stage.setOnCloseRequest((e) -> {
            e.consume();
            cancel();
        });
        stage.setScene(new Scene(box = new VBox(10)));
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        box.setPadding(new Insets(5, 10, 5, 10));
        HBox hb;
        box.getChildren().addAll(new Label("New Project"),
                projectName = new TextField(getDefaultProjectName()),
                projectPath = new TextField(),
                new Label("Package Name"),
                packageName = new TextField(),
                new Label("Main Class Name"),
                mainClassName = new TextField(),
                hb = new HBox(5,
                        cancel = new Button("Cancel"),
                        confirm = new Button("Confirm")));
        projectName.setPromptText("Project Name");
        projectPath.setPromptText("Project File Path");
        packageName.setPromptText("Package Name");
        mainClassName.setPromptText("Main-Class Name");
        box.setAlignment(Pos.CENTER);
        hb.setAlignment(Pos.CENTER_RIGHT);
        projectPath.setText(Dependencies.workplace_location + File.separator + projectName.getText());
        projectPath.setEditable(false);
        projectName.textProperty().addListener((ob, older, newer) -> {
            projectPath.setText(Dependencies.workplace_location + File.separator + newer);
        });
        cancel.setOnAction((e) -> {
            cancel();
        });
        mainClassName.setOnAction((e) -> {
            confirm.fire();
        });
        confirm.setOnAction((e) -> {
            if (projectName.getText().length() > 0) {
                if (verify(projectName.getText())) {
                    if (!mainClassName.getText().isEmpty()) {
                        if (!mainClassName.getText().contains(".")) {
                            if (valid(packageName.getText())) {
                                confirmed = true;
                                stage.close();
                            } else {
                                Writer.showAlert(AlertType.ERROR, stage, "Package Name", "Package name is not valid!", "");
                            }
                        } else {
                            Writer.showAlert(AlertType.ERROR, stage, "Main-Class Name", "Main Class cannot contain character \".\"", "");
                        }
                    } else {
                        Writer.showAlert(AlertType.ERROR, stage, "Error", "Project", "Main-Class Name cannot be empty!");
                    }
                } else {
                    Writer.showAlert(AlertType.ERROR, stage, "New Project", "Project : " + projectName.getText(), "Project already exists!");
                }
            } else {
                Writer.showAlert(AlertType.ERROR, stage, "Error", "Project", "Project name cannot be empty!");
            }
        });
    }

    private boolean valid(String packageName) {
        for (char c : packageName.toCharArray()) {
            if (!Character.isAlphabetic((int) c) && !(c == '.')) {
                return false;
            }
        }
        if (packageName.charAt(0) == '.' || packageName.charAt(packageName.length() - 1) == '.') {
            return false;
        }
        for (int x = 0; x < packageName.length() - 1; x++) {
            if (packageName.charAt(x) == '.' && packageName.charAt(x + 1) == '.') {
                return false;
            }
        }
        for (String s : hardSplit(packageName, ".")) {
            for (String sa : Highlighter.JAVA_KEYWORDS) {
                if (sa.equals(s)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> hardSplit(String s, String a) {
        ArrayList<String> al = new ArrayList<>();
        while (s.contains(a)) {
            String one = s.substring(0, s.indexOf(a));
            s = s.substring(s.indexOf(a) + 1);
            al.add(one);
        }
        return al;
    }

    private void cancel() {
        confirmed = false;
        stage.close();
    }

    private boolean verify(String s) {
        File f = new File(s);
        return !f.exists();
    }

    private boolean confirmed;

    public List<String> showAndWait() {
        stage.showAndWait();
        if (confirmed) {
            return FXCollections.observableArrayList(projectPath.getText(),
                    (packageName.getText().isEmpty() ? "" : (packageName.getText()
                            + ".")) + mainClassName.getText());
        }
        return new ArrayList<>();
    }

    private String getDefaultProjectName() {
        int x = 1;
        File f = new File(Dependencies.workplace_location + File.separator + "JavaProject" + x);
        if (f.exists()) {
            while (f.exists()) {
                x++;
                f = new File(Dependencies.workplace_location + File.separator + "JavaProject" + x);
            }
        }
        return f.getName();
    }
}
