/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import static tachyon.Tachyon.icon;

/**
 *
 * @author Aniket
 */
public class ProjectOptions {

    private final Stage stage;
    private final ListView<String> list;
    private final BorderPane main;
    int ret;

    public ProjectOptions(Window w) {
        stage = new Stage();
        stage.setTitle("Project Type");
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
        list = new ListView<>();
        stage.setScene(new Scene(main = new BorderPane(list)));
        main.setPadding(new Insets(5, 10, 5, 10));
        list.getItems().addAll("Java Standard Project",
                "JavaFx Application Standard Project");
        list.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                if (list.getSelectionModel().getSelectedItem() != null) {
                    ret = list.getItems().indexOf(list.getSelectionModel().getSelectedItem());
                    finish();
                }
            }
        });
        Button cancel, finish;
        HBox hob;
        main.setBottom(hob = new HBox(15));
        hob.setPadding(new Insets(5, 10, 5, 10));
        hob.setAlignment(Pos.CENTER_RIGHT);
        hob.getChildren().addAll(cancel = new Button("Cancel"), finish = new Button("Next"));
        cancel.setOnAction((e) -> {
            cancel();
        });
        finish.setOnAction((e) -> {
            if (list.getSelectionModel().getSelectedItem() != null) {
                ret = list.getItems().indexOf(list.getSelectionModel().getSelectedItem());
                finish();
            } else {
                Alert al = new Alert(AlertType.ERROR);
                al.initOwner(stage);
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(Tachyon.icon);
                al.setTitle("Error");
                al.setHeaderText("You must select a Project Configuration");
                al.showAndWait();
            }
        });

        ret = -1;
    }

    public final void finish() {
        stage.close();
    }

    public final void cancel() {
        ret = -1;
        stage.close();
    }

    public int showAndWait() {
        stage.showAndWait();
        return ret;
    }
}
