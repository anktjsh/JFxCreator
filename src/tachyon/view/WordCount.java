/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;
import tachyon.core.Program;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class WordCount {

    public static void getWordCount(Window w, Program pro) {
        Stage stage = getWindow(w);
        BorderPane pane = (BorderPane) stage.getScene().getRoot();
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(box, Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        pane.setCenter(box);

        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(pro.getFile()));
        } catch (IOException ex) {
        }
        box.getChildren().addAll(new Label("Program  : " + pro.getClassName()));
        box.getChildren().addAll(new Label("Lines : " + al.size()));
        StringBuilder sb = new StringBuilder();
        for (String s : al) {
            sb.append(s).append("\n");
        }
        String total = sb.toString();
        box.getChildren().addAll(new Label("Characters : " + total.length()),
                new Label("Characters/Line : " + (double) total.length() / al.size()));
        String words[] = total.split("\\W");
        box.getChildren().addAll(new Label("Words : " + words.length),
                new Label("Words/Lines : " + (double) words.length / al.size()));
        stage.showAndWait();
    }

    public static void getWordCount(Window w, Project pro) {
        Stage stage = getWindow(w);
        BorderPane pane = (BorderPane) stage.getScene().getRoot();
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(box, Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        pane.setCenter(box);
        box.getChildren().addAll(new Label("Project : " + pro.getProjectName()),
                new Label("Total Programs : " + pro.getPrograms().size()));

        int characters = 0;
        int words = 0;
        int lines = 0;
        for (Program p : pro.getPrograms()) {
            ArrayList<String> al = new ArrayList<>();
            try {
                al.addAll(Files.readAllLines(p.getFile()));
            } catch (IOException ex) {
            }
            lines += al.size();
            StringBuilder sb = new StringBuilder();
            for (String s : al) {
                sb.append(s).append("\n");
            }
            String total = sb.toString();
            characters += total.length();
            String[] word = total.split("\\W");
            words += word.length;
        }

        box.getChildren().addAll(new Label("Lines : " + lines),
                new Label("Words : " + words),
                new Label("Characters : " + characters),
                new Label("Characters/Line : " + (double) characters / lines),
                new Label("Words/Line : " + (double) words / lines));
        stage.showAndWait();
    }

    public static Stage getWindow(Window w) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(w);
        stage.setTitle("Word Count");
        stage.setResizable(false);
        stage.setWidth(400);
        stage.setHeight(400);
        stage.getIcons().add(tachyon.Tachyon.icon);
        stage.setScene(new Scene(new BorderPane()));
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        return stage;
    }
}
