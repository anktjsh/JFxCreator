/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.PrintStream;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import jfxcreator.core.ProcessPool.ProcessItem;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class ConsoleWindow extends Tab {

    private final ProcessItem console;
    private final TextArea area;
    private PrintStream printer;

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
        console = c;
        area = new TextArea();
        if (c.getProcess() == null) {
            c.processProperty().addListener((ob, older, newer) -> {
                printer = new PrintStream(newer.getOutputStream());
            });
        } else {
            printer = new PrintStream(c.getProcess().getOutputStream());
        }

        area.setFont(Writer.fontSize.get());
        Writer.fontSize.addListener((ob, older, newer) -> {
            area.setFont(newer);
        });
        Writer.wrapText.addListener((ob, older, neweer) -> {
            area.setWrapText(neweer);
        });

        setContent(area);
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
    }

    private void appendAll(List<Character> al) {
        for (char s : al) {
            append(s);
        }
    }

    private void append(char s) {
        if (Platform.isFxApplicationThread()) {
            area.appendText(s + "");
            length = area.getText().length();
        } else {
            Platform.runLater(() -> {
                area.appendText(s + "");
                length = area.getText().length();
            });
        }
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
                System.out.println(s);
                printer.println(s);
                printer.flush();
                length = area.getText().length();
            }
        }
        if (ke.getCode() == KeyCode.BACK_SPACE) {
            ke.consume();
        }
        if (ke.getCode() == KeyCode.F) {
            if (ke.isControlDown()) {
                
            }
        }
    }
}
