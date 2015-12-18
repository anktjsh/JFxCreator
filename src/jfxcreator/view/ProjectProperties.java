/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
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

    public ProjectProperties(Project project, Window w) {
        this.project = project;
        stage = new Stage();
        stage.initOwner(w);
        stage.setWidth(400);
        stage.setHeight(400);
        stage.setTitle("Project Properties");
        stage.getIcons().add(jfxcreator.JFxCreator.icon);
        stage.setResizable(false);
        stage.setScene(new Scene(box = new VBox(15,
                new HBox(10, mainClass = new TextField(project.getMainClassName()), select = new Button("Select")), 
        new HBox(10, cancel = new Button("Cancel"), confirm = new Button("Confirm")))));
        box.setPadding(new Insets(5, 10, 5, 10));
        mainClass.setEditable(false);
        select.setOnAction((e) -> {
            List<String> all  = getAll();
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
            stage.close();
        });
    }
    
    private List<String> getAll() {
        ArrayList<String> al = new ArrayList<>();
        for (Program pr : project.getPrograms()) {
            List<String> str = pr.getLastCode();
            ArrayList<String> ret = new ArrayList<>();
            for (String s : str) {
                if (s.contains("public")&&s.contains("static")&&s.contains("void")&&s.contains("main")) {
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
