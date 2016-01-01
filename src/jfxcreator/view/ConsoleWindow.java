/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import jfxcreator.JFxCreator;
import jfxcreator.core.ProcessPool.ProcessItem;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class ConsoleWindow extends Tab {

    private final ProcessItem console;
    private final JTextArea jArea;
    private PrintStream printer;
    private final BorderPane center, bottom;
    private final Button cancel;
    private int length;

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

        jArea = new JTextArea();
        jArea.setFont(jArea.getFont().deriveFont((float) Writer.fontSize.getValue().getSize()));
        Writer.fontSize.addListener((ob, older, newer) -> {
            jArea.setFont(jArea.getFont().deriveFont((float) newer.getSize()));
        });
        Writer.wrapText.addListener((ob, older, neweer) -> {
            jArea.setWrapStyleWord(neweer);
        });
        SwingNode node = new SwingNode();

        JPanel main = new JPanel(new BorderLayout());
        main.add(new JScrollPane(jArea), BorderLayout.CENTER);
        DefaultCaret caret = (DefaultCaret) jArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        node.setContent(main);
        setContent(center = new BorderPane(node));

        appendAll(console.getConsole().getList());

        console.getConsole().getList().addListener((ListChangeListener.Change<? extends Character> c1) -> {
            c1.next();
            if (c1.wasAdded()) {
                for (char s : c1.getAddedSubList()) {
                    append(s);
                }
            }
        });
        bindKeyListeners();

        center.setBottom(bottom = new BorderPane());
        bottom.setPadding(new Insets(5, 10, 5, 10));
        bottom.setLeft(cancel = new Button("Cancel Process"));

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
                }
            }
        });

        setOnCloseRequest((e) -> {
            if (console.getProcess().isAlive()) {
                Alert al = new Alert(AlertType.CONFIRMATION);
                al.setTitle(console.getName());
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(jfxcreator.JFxCreator.icon);
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
        });
    }

    public void complete(String process) {
        SwingUtilities.invokeLater(() -> {
            jArea.append("\n" + process + " Complete\n");
            length = jArea.getText().length();
        });
    }

    private void appendAll(List<Character> al) {
        for (char s : al) {
            append(s);
        }
    }

    private void append(char s) {
        if (console.getProcess().isAlive() || !(console.getName().contains("Launching") && console.getName().contains("Jar"))) {
            SwingUtilities.invokeLater(() -> {
                jArea.append(s + "");
                length = jArea.getText().length();
            });
        } else {
            System.out.print(s);
        }
    }

    private void bindKeyListeners() {
        jArea.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                type(jArea.getText(), console.getConsole().getProject(), e);
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
            }
        });
    }

    public void type(String st, Project proj, KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if (console.getProcess().isAlive()) {
                String s = st.substring(length == 0 ? 0 : (length));
                printer.println(s);
                printer.flush();
                length = jArea.getText().length();
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            ke.consume();
        }
        if (ke.getKeyCode() == KeyEvent.VK_F) {
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
                    if (jArea.getSelectedText() == null || jArea.getSelectedText().length() == 0) {
                        String a = fi.getText();
                        int index = jArea.getText().indexOf(a);
                        if (index != -1) {
                            jArea.select(index, index + a.length());
                        }
                    } else {
                        next.fire();
                    }
                });
                prev.setOnAction((efd) -> {
                    int start = jArea.getSelectionStart();
                    String a = jArea.getText().substring(0, start);
                    int index = a.lastIndexOf(fi.getText());
                    if (index != -1) {
                        jArea.select(index, index + fi.getText().length());
                    }
                });
                next.setOnAction((sdfsdfsd) -> {
                    int end = jArea.getSelectionEnd();
                    String a = jArea.getText().substring(end);
                    int index = a.indexOf(fi.getText());
                    if (index != -1) {
                        index += end;
                        jArea.select(index, index + fi.getText().length());
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
