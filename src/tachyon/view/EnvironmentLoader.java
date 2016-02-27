/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.ProgressNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tachyon.Tachyon;

/**
 * Simple Preloader Using the ProgressBar Control
 *
 * @author Aniket
 */
public class EnvironmentLoader extends Preloader {

    ProgressBar bar;
    Stage stage;

    private Scene createPreloaderScene() {
        bar = new ProgressBar();
        BorderPane p = new BorderPane();
        p.setPadding(new Insets(5, 10, 5, 10));
        BorderPane c = new BorderPane();
        p.setCenter(c);
        c.setCenter(bar);
        ImageView im;
        c.setTop(im = new ImageView(Tachyon.icon));
        im.setFitHeight(256);
        im.setFitWidth(256);
        Label one, two;
        p.setTop(one = new Label("Welcome to Tachyon"));
        p.setBottom(two = new Label("Loading..."));
        one.setFont(new Font(15));
        two.setFont(new Font(15));
        bar.setPrefWidth(350);
        BorderPane.setAlignment(p.getTop(), Pos.CENTER);
        BorderPane.setAlignment(p.getBottom(), Pos.CENTER);
        BorderPane.setAlignment(c.getCenter(), Pos.CENTER);
        BorderPane.setAlignment(p.getCenter(), Pos.CENTER);
        BorderPane.setAlignment(c.getTop(), Pos.CENTER);
        Scene sc = new Scene(p, 500, 350);
        return sc;
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(Tachyon.icon);
        stage.setOnCloseRequest((e) -> {
            e.consume();
        });
        stage.setScene(createPreloaderScene());
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification scn) {
        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                Platform.runLater(() -> {
                    stage.hide();
                });
            }).start();
        }
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        bar.setProgress(pn.getProgress());
    }

}
