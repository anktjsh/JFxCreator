/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jfxcreator.JFxCreator;
import jfxcreator.core.ProcessPool.ProcessItem;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class ConsoleWindow extends Tab {

    private final ProcessItem console;
    private PrintStream printer;
    private final BorderPane center, bottom;
    private final Button cancel;
    private int length;

    private final TextTimer timer;
    private final TextArea area;
    private String currentText = "";

    public ConsoleWindow(ProcessItem c) {
        if (c.getName() == null) {
            c.nameProperty().addListener((ob, older, newer) -> {
                Platform.runLater(() -> {
                    setText(newer);
                });
            });
        } else {
            setText(c.getName());
        }
        setContextMenu(new ContextMenu());
        console = c;
        if (console != null) {
            console.getConsole().setConsoleWindow(this);
        }
        if (c.getProcess() == null) {
            c.processProperty().addListener((ob, older, newer) -> {
                printer = new PrintStream(newer.getOutputStream());
            });
        } else {
            printer = new PrintStream(c.getProcess().getOutputStream());
        }

        getContextMenu().getItems().addAll(new MenuItem("Close"),
                new MenuItem("Close All"));
        getContextMenu().getItems().get(0).setOnAction((e) -> {
            getTabPane().getTabs().remove(this);
        });
        getContextMenu().getItems().get(1).setOnAction((e) -> {
            getTabPane().getTabs().clear();
        });

        setContent(center = new BorderPane(area = new TextArea()));

        Writer.fontSize.addListener((ob, older, newer) -> {
            area.setFont(newer);
        });
        Writer.wrapText.addListener((ob, older, neweer) -> {
            area.setWrapText(neweer);
        });

        appendAll(console.getConsole().getList());

        console.getConsole().getList().addListener((ListChangeListener.Change<? extends Character> c1) -> {
            c1.next();
            if (c1.wasAdded()) {
                for (char s : c1.getAddedSubList()) {
                    addToQueue(s);
                }
            }
        });
        bindKeyListeners();

        center.setBottom(bottom = new BorderPane());
        bottom.setPadding(new Insets(5, 10, 5, 10));
        bottom.setLeft(cancel = new Button("Cancel Process"));
        (timer = new TextTimer()).start();
        cancel.setOnAction((e) -> {
            Alert al = new Alert(AlertType.CONFIRMATION);
            al.setTitle("End Process");
            al.setHeaderText("Are you sure you want to end the process?");
            al.initOwner(getTabPane().getScene().getWindow());
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            Optional<ButtonType> show = al.showAndWait();
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    try {
                        console.getProcess().getOutputStream().close();
                    } catch (IOException ex) {
                    }
                    if (console.getProcess().isAlive()) {
                        console.getProcess().destroyForcibly();
                    }
                    cancel.setDisable(true);
                    timer.stop();
                }
            }
        });
        setOnCloseRequest((e) -> {
            if (console.getProcess().isAlive()) {
                Alert al = new Alert(AlertType.CONFIRMATION);
                al.setTitle(console.getName());
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
                al.setHeaderText("Cancel Process");
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                        cancel.fire();
                    } else {
                        e.consume();
                    }
                } else {
                    e.consume();
                }
            }
            if (!e.isConsumed()) {
                timer.stop();
            }
        });

    }

    private void addToQueue(Character c) {
        currentText += c;
    }

    private class TextTimer extends AnimationTimer {

        public TextTimer() {
        }

        @Override
        public void handle(long now) {
            if (!currentText.isEmpty()) {
                append(currentText);
                currentText = "";
            }
        }

    }

    public void complete(String process) {
        Platform.runLater(() -> {
            append("\n" + process + " Complete\n");
        });
    }

    private void appendAll(List<Character> al) {
        for (char s : al) {
            currentText += s;
        }
    }

    private void append(String s) {
        area.appendText(s);
        length = area.getText().length();
    }

    private void bindKeyListeners() {
        area.setOnKeyPressed((e) -> {
            type(area.getText(), console.getConsole().getProject(), e);
        });
    }

    public void type(String st, Project proj, KeyEvent ke) {
        if (ke.getCode() == KeyCode.ENTER) {
            if (console.getProcess().isAlive()) {
                String s = st.substring(length == 0 ? 0 : (length));
                printer.println(s);
                printer.flush();
                length = area.getText().length();
                System.out.println(length);
            }
        }
        if (ke.getCode() == KeyCode.BACK_SPACE) {
            ke.consume();
        }
        if (ke.getCode() == KeyCode.F) {
            if (ke.isControlDown()) {
                HBox box = new HBox(15);
                BorderPane main = new BorderPane(box);
                box.setPadding(new Insets(5, 10, 5, 10));
                box.setStyle("-fx-background-fill:gray;");
                TextField fi;
                Button prev, next;
                box.getChildren().addAll(fi = new TextField(),
                        prev = new Button("Previous"),
                        next = new Button("Next"));
                fi.setPromptText("Find");
                fi.setOnAction((ea) -> {
                    if (area.getSelectedText() == null || area.getSelectedText().length() == 0) {
                        String a = fi.getText();
                        int index = area.getText().indexOf(a);
                        if (index != -1) {
                            area.selectRange(index, index + a.length());
                        }
                    } else {
                        next.fire();
                    }
                });
                prev.setOnAction((efd) -> {
                    int start = area.getSelection().getStart();
                    String a = area.getText().substring(0, start);
                    int index = a.lastIndexOf(fi.getText());
                    if (index != -1) {
                        area.selectRange(index, index + fi.getText().length());
                    }
                });
                next.setOnAction((sdfsdfsd) -> {
                    int end = area.getSelection().getEnd();
                    String a = area.getText().substring(end);
                    int index = a.indexOf(fi.getText());
                    if (index != -1) {
                        index += end;
                        area.selectRange(index, index + fi.getText().length());
                    }
                });
                Button close;
                main.setRight(close = new Button("X"));
                BorderPane.setMargin(main.getRight(), new Insets(5, 10, 5, 10));
                Platform.runLater(() -> {
                    bottom.setTop(main);
                    fi.requestFocus();
                    close.setOnAction((se) -> {
                        bottom.setTop(null);
                    });
                });
            }
        }
    }
}
