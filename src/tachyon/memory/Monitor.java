/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.memory;

import java.text.DecimalFormat;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;
import tachyon.memory.MemoryWatcher.ThreadStatus;

/**
 *
 * @author Aniket
 */
public class Monitor {

    private static Monitor monitor;
    private final Stage stage;
    private final HBox box;
    private final BorderPane pane;
    private final Label max, threadCount;
    private final ListView<ThreadStatus> threads;
    private final BorderPane bottom;

    private Monitor(Window w) {
        stage = new Stage();
        stage.setTitle("Tachyon Memory Monitor");
        stage.getIcons().add(Tachyon.icon);
        stage.initOwner(w);
        stage.setScene(new Scene(pane = new BorderPane(box = new HBox(15))));
        BorderPane.setAlignment(box, Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        stage.setOnCloseRequest((e) -> {
            stage.hide();
            e.consume();
        });
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
        box.setAlignment(Pos.CENTER);
        ListView<String> one, two, three;
        Label cOne, cTwo, cThree;
        MemoryWatcher watch = new MemoryWatcher();
        pane.setTop(max = new Label("Max Memory : " + getMegaBytes(watch.getMaxMemory())));
        BorderPane.setMargin(pane.getTop(), new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(max, Pos.CENTER);
        box.getChildren().addAll(
                new VBox(10,
                        new Label("Current Memory Available"),
                        one = new ListView<>(),
                        cOne = new Label(getMegaBytes(watch.freeMemoryProperty().getValue()))),
                new VBox(10,
                        new Label("Used Memory"),
                        two = new ListView<>(),
                        cTwo = new Label(getMegaBytes(watch.usedMemoryProperty().getValue()))),
                new VBox(10,
                        new Label("Total Memory"),
                        three = new ListView<>(),
                        cThree = new Label(getMegaBytes(watch.totalMemoryProperty().getValue()))));
        for (Node n : box.getChildren()) {
            if (n instanceof VBox) {
                VBox v = (VBox) n;
                v.setAlignment(Pos.CENTER);
            }
        }
        one.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            c.next();
            if (!c.getList().isEmpty()) {
                one.scrollTo(c.getList().size() - 1);
            }
        });
        two.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            c.next();
            if (!c.getList().isEmpty()) {
                two.scrollTo(c.getList().size() - 1);
            }
        });
        three.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            c.next();
            if (!c.getList().isEmpty()) {
                three.scrollTo(c.getList().size() - 1);
            }
        });
        watch.freeList().addListener((ListChangeListener.Change<? extends Pair<LocalTime, Long>> c) -> {
            c.next();
            if (c.wasAdded()) {
                run(() -> {
                    for (Pair<LocalTime, Long> l : c.getAddedSubList()) {
                        one.getItems().add(l.getKey().toString() + " : " + getMegaBytes(l.getValue()));
                    }
                });
            }
        });
        watch.usedList().addListener((ListChangeListener.Change<? extends Pair<LocalTime, Long>> c) -> {
            c.next();
            if (c.wasAdded()) {
                run(() -> {
                    for (Pair<LocalTime, Long> l : c.getAddedSubList()) {
                        two.getItems().add(l.getKey().toString() + " : " + getMegaBytes(l.getValue()));
                    }
                });
            }
        });
        watch.totalList().addListener((ListChangeListener.Change<? extends Pair<LocalTime, Long>> c) -> {
            c.next();
            if (c.wasAdded()) {
                run(() -> {
                    for (Pair<LocalTime, Long> l : c.getAddedSubList()) {
                        three.getItems().add(l.getKey().toString() + " : " + getMegaBytes(l.getValue()));
                    }
                });
            }
        });
        watch.freeMemoryProperty().addListener((ob, older, newer) -> {
            run(() -> {
                cOne.setText(getMegaBytes(newer.longValue()));
            });
        });
        watch.usedMemoryProperty().addListener((ob, older, newer) -> {
            run(() -> {
                cTwo.setText(getMegaBytes(newer.longValue()));
            });
        });
        watch.totalMemoryProperty().addListener((ob, older, newer) -> {
            run(() -> {
                cThree.setText(getMegaBytes(newer.longValue()));
            });
        });
        watch.maxMemoryProperty().addListener((ob, older, newer) -> {
            run(() -> {
                max.setText("Max Memory : " + getMegaBytes(newer.longValue()));
            });
        });
        threads = new ListView<>();
        threads.setMaxHeight(200);
        watch.getStatusList().addListener((ListChangeListener.Change<? extends MemoryWatcher.ThreadStatus> c) -> {
            c.next();
            if (c.wasAdded()) {
                run(() -> {
                    refreshThreads(watch);
                });
            }
        });
        bottom = new BorderPane(threads);
        pane.setBottom(bottom);
        bottom.setPadding(new Insets(5, 10, 5, 10));
        bottom.setBottom(threadCount = new Label(""));
        BorderPane.setAlignment(threadCount, Pos.CENTER);
        (new Thread(watch)).start();
    }

    private void refreshThreads(MemoryWatcher watch) {
        threads.getItems().clear();
        threads.getItems().addAll(watch.getStatusList());
        threadCount.setText("Total Threads : " + watch.getStatusList().size());
    }

    public static DecimalFormat df = new DecimalFormat("0.00");

    private String getMegaBytes(long l) {
        double meg = l / (double) (1024 * 1024);
        return df.format(meg) + " MegaBytes";
    }

    private void run(Runnable run) {
        if (stage.isShowing()) {
            Platform.runLater(run);
        } else {
            run.run();
        }
    }

    public static void show() {
        monitor.showAndWait();
    }

    public void showAndWait() {
        stage.show();
    }

    public static void initialize(Window w) {
        if (monitor != null) {
            throw new RuntimeException("Already created");
        } else {
            monitor = new Monitor(w);
        }
    }
}
