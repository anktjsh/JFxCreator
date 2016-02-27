/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.contact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import tachyon.view.Writer;

/**
 *
 * @author Aniket
 */
public class EmailPicker {

    private final Stage stage;
    private final String subject;

    public EmailPicker(Window w, String sub) {
        subject = sub;
        stage = new Stage();
        stage.initOwner(w);
        stage.setTitle("Email");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(Tachyon.icon);
        stage.setResizable(false);
        stage.setScene(new Scene(new EmailPane()));
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    private class EmailPane extends BorderPane {

        private final BorderPane top, bottom, center;

        private final Button cancel, send;
        private final TextArea message;
        private final VBox box, creds;
        private final TextField username;
        private final PasswordField password;
        private final HBox options;
        private final Button gmail, hotmail;
        private final Label extension;

        public EmailPane() {
            setPadding(new Insets(5, 10, 5, 10));
            top = new BorderPane();
            top.setPadding(getPadding());
            bottom = new BorderPane();
            bottom.setPadding(getPadding());
            center = new BorderPane();
            center.setPadding(getPadding());
            setTop(top);
            setBottom(bottom);
            setCenter(center);

            bottom.setLeft(cancel = new Button("Cancel"));
            bottom.setRight(send = new Button("Send"));
            cancel.setOnAction((e) -> {
                ((Stage) getScene().getWindow()).close();
            });
            send.setDisable(true);
            center.setCenter(message = new TextArea());
            message.setDisable(true);

            box = new VBox(5);
            box.setPadding(getPadding());
            box.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(new Label("To : serpior.ariad@gmail.com"),
                    new Label("Subject : " + subject));
            center.setTop(box);

            creds = new VBox(5);
            creds.setPadding(getPadding());
            creds.getChildren().addAll(
                    new HBox(5,
                            new Label("Username : "),
                            username = new TextField(),
                            extension = new Label("")),
                    new HBox(5,
                            new Label("Password : "),
                            password = new PasswordField()));
            creds.setAlignment(Pos.CENTER);
            username.setDisable(true);
            password.setDisable(true);
            top.setBottom(creds);

            top.setTop(options = new HBox(15));
            options.setAlignment(Pos.CENTER);
            options.setPadding(getPadding());
            options.getChildren().addAll(gmail = new Button("Gmail"),
                    hotmail = new Button("Hotmail"));
            gmail.setOnAction((e) -> {
                extension.setText("@gmail.com");
                gmail.setDisable(true);
                hotmail.setDisable(true);
                send.setDisable(false);
                message.setDisable(false);
                username.setDisable(false);
                password.setDisable(false);
            });
            hotmail.setOnAction((e) -> {
                extension.setText("@hotmail.com");
                gmail.setDisable(true);
                hotmail.setDisable(true);
                send.setDisable(false);
                message.setDisable(false);
                username.setDisable(false);
                password.setDisable(false);
            });

            send.setOnAction((e) -> {
                Email.Status sent = EmailFactory.newEmailFactory().setCredentials(
                        extension.getText().contains("gmail") ? EmailFactory.GMAIL : EmailFactory.HOTMAIL,
                        username.getText() + extension.getText(),
                        password.getText())
                        .addRecipient("serpior.ariad@gmail.com")
                        .setSubject(subject)
                        .setMessage(message.getText())
                        .construct().send();
                System.out.println("hello");
                if (!sent.isSuccess()) {
                    if (sent.getMessage().contains("Username and Password not accepted")) {
                        error(sent.getMessage());
                    } else if (sent.getMessage().contains("https://accounts.google.com/ContinueSignIn")) {
                        error("You must allow Tachyon to access your gmail account");
                        getScene().setRoot(new GmailVerifier(this));
                    } else {
                        error(sent.getMessage());
                    }
                } else {
                    try {
                        Files.write(Paths.get(".cache" + File.separator + "emailstamp.txt"),
                                FXCollections.observableArrayList(LocalDate.now().toString(),
                                        LocalTime.now().toString()));
                    } catch (IOException ex) {
                    }
                    Writer.showAlert(AlertType.INFORMATION,stage,"Email Sent","Email Sent","");
                    stage.close();
                }

            });
        }

    }

    private void error(String message) {
        Writer.showAlert(AlertType.ERROR, stage, "Error Sending", message, "");
    }
}
