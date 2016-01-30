/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.contact;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 *
 * @author Aniket
 */
public class GmailVerifier extends BorderPane {

    private final BorderPane bottom;
    private final Button back;
    private final WebView view;
    private final VBox box;

    public GmailVerifier(BorderPane previous) {
        bottom = new BorderPane();
        bottom.setPadding(new Insets(5, 10, 5, 10));
        setBottom(bottom);
        bottom.setLeft(back = new Button("<Back"));
        back.setOnAction((e) -> {
            getScene().setRoot(previous);
        });
        view = new WebView();
        setCenter(view);
        view.getEngine().load("https://www.google.com/settings/security/lesssecureapps");
        box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        setTop(box);
        BorderPane.setAlignment(box, Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.getChildren().addAll(new Label("Instructions : "),
                new Label("To enable JFxCreator to send an email,"),
                new Label("login and allow your email to be accessed from less secure apps"),
                new Label("If you do not want to do this, click back,"),
                new Label("and then click cancel to exit the email dialog"));
    }
}
