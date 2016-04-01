/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;

/**
 *
 * @author Aniket
 */
public class ErrorConsole {

    private static ErrorConsole error;
    private final Stage stage;
    private final ListView<Error> view;

    public ErrorConsole(Window w) {
        stage = new Stage();
        stage.setTitle("Error Console");
        stage.initOwner(w);
        stage.setWidth(500);
        view = new ListView<>();
        stage.setScene(new Scene(view));
        view.setCellFactory((param) -> new ErrorCell());
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        Tachyon.applyCss.addListener((ob, older, newer) -> {
            if (newer) {
                stage.getScene().getStylesheets().add(css);
            } else {
                stage.getScene().getStylesheets().remove(css);
            }
        });
    }

    private class ErrorCell extends ListCell<Error> {

        private final HBox graphic;
        private final Button show, remove;
        private final Label label;
        private Error error;

        public ErrorCell() {
            graphic = new HBox(5);
            graphic.getChildren().addAll(
                    label = new Label(""),
                    show = new Button("Show StackTrace"),
                    remove = new Button("X"));
            remove.setStyle(
                    "-fx-background-radius: 5em; "
                    + "-fx-min-width: 30px; "
                    + "-fx-min-height: 30px; "
                    + "-fx-max-width: 30px; "
                    + "-fx-max-height: 30px;"
            );
            remove.setOnAction((e) -> {
                if (error != null) {
                    view.getItems().remove(error);
                }
            });
            show.setOnAction((e) -> {
                if (error != null) {
                    showException(error.getThrowable(), stage, error.getIdentifier());
                }
            });
        }

        @Override
        protected void updateItem(Error item, boolean empty) {
            if (item != null) {
                error = item;
                label.setText("Exception in Thread " + error.getThread().getName());
                setGraphic(graphic);
            } else {
                setGraphic(null);
            }
        }
    }

    private static void showException(Throwable th, Stage stage, String ide) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(th.getClass().getName());
        if (ide == null) {
            alert.setHeaderText(th.getMessage());
        } else {
            alert.setHeaderText(ide);
            alert.setContentText(th.getMessage());
        }
        alert.initOwner(stage);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    public static void show() {
        error.showAndWait();
    }

    public void showAndWait() {
        stage.show();
    }

    public static void initialize(Window w) {
        if (error != null) {
            throw new RuntimeException("Already Created");
        } else {
            error = new ErrorConsole(w);
        }
    }

    public static ObservableList<Error> getErrors() {
        return error.view.getItems();
    }

    private void add(Error r) {
        view.getItems().add(r);
    }

    public static void addError(Thread d, Throwable t, String id) {
        if (Platform.isFxApplicationThread()) {
            error.add(new Error(d, t, id));
        } else {
            Platform.runLater(() -> {
                error.add(new Error(d, t, id));
            });
        }
    }

    public static class Error {

        private final Thread thr;
        private final Throwable thro;
        private final String identifier;

        public Error(Thread t, Throwable th, String id) {
            thr = t;
            thro = th;
            identifier = id;
        }

        public String getIdentifier() {
            return identifier;
        }

        public Thread getThread() {
            return thr;
        }

        public Throwable getThrowable() {
            return thro;
        }
    }
}
