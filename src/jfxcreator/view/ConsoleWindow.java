/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import jfxcreator.JFxCreator;
import jfxcreator.core.Console;
import jfxcreator.core.JavaPlatform;
import jfxcreator.core.ProcessItem;
import jfxcreator.core.Program;
import jfxcreator.core.Project;
import static jfxcreator.view.Writer.fontSize;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.Paragraph;

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
    private final CodeArea area;
    private String currentText = "";
    private final TreeMap<Integer, Integer> errorOutput;
    private final TreeMap<Integer, Integer> stackTraces;
    private final TreeMap<Integer, Integer> fullError;
    private final TreeMap<Integer, Integer> fullStack;

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

        setContent(center = new BorderPane(area = new CodeArea(), null, null, null, null));

        Popup popup = new Popup();
        Label popupMsg = new Label();
        popupMsg.setStyle(
                "-fx-background-color: black;"
                + "-fx-text-fill: white;"
                + "-fx-padding: 5;");
        popup.getContent().add(popupMsg);

        area.setMouseOverTextDelay(Duration.ofMillis(50));
        area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
            int chIdx = e.getCharacterIndex();
            Collection<String> stlye = area.getStyleAtPosition(chIdx);
            if (stlye.contains("red") && stlye.contains("stacktrace")) {
                Point2D pos = e.getScreenPosition();
                popupMsg.setText("Click to go to Source");
                popup.show(area, pos.getX(), pos.getY() + 10);
            }
        });
        area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            popup.hide();
        });
        area.getStylesheets().add(JFxCreator.class.getResource("other.css").toExternalForm());
        area.setOnMouseClicked((e) -> {
            int chIdx = area.getCaretPosition();
            Collection<String> stlye = area.getStyleAtPosition(chIdx);
            if (stlye.contains("red") && stlye.contains("stacktrace")) {
                try {
                    String text = area.getParagraph(getRow(chIdx)).toString().trim();
                    String[] arr = (text.split(" "));
                    if (arr.length > 0) {
                        if (arr[0].equals("at")) {
                            String inParen = arr[1].substring(arr[1].lastIndexOf("(") + 1, arr[1].lastIndexOf(")"));
                            String[] spl = inParen.split(":");
                            String className = spl[0].substring(0, spl[0].indexOf(".java"));
                            String beforeParen = arr[1].substring(0, arr[1].lastIndexOf("("));
                            //HERE
                            int lineNum = Integer.parseInt(spl[1]);
                            String fullName = beforeParen.substring(0, beforeParen.lastIndexOf(className)) + className;
//HERE
                            Program chosen = null;
                            for (Program p : console.getConsole().getProject().getPrograms()) {
                                if (p.getClassName().equals(fullName)) {
                                    System.out.println(p.getClassName());
                                    chosen = p;
                                }
                            }
                            if (chosen != null) {
                                Parent parent = getTabPane().getParent().getParent();
                                Writer w = (Writer) parent;
                                w.addEditorFromStackTrace(chosen, lineNum);
                                System.out.println(parent.getClass().getName());
                            } else {
                                InputStream stream = JavaPlatform.getCurrentPlatform().getInputStream(fullName);
                                if (stream != null) {
                                    Parent parent = getTabPane().getParent().getParent();
                                    Writer w = (Writer) parent;
                                    w.addClassReader(console.getConsole().getProject(), getEntryPath(fullName), stream, lineNum);
                                }
                            }
                        } else {
                            Program chosen = null;
                            Integer line = null;
                            String loc[] = arr[0].split(":");
                            String OS = System.getProperty("os.name").toLowerCase();
                            if (OS.contains("win")) {
                                File location = new File("C:" + loc[1]);
                                for (Program pro : console.getConsole().getProject().getPrograms()) {
                                    if (pro.getFile().toAbsolutePath().toString().equals(location.getAbsolutePath())) {
                                        System.out.println(pro.getClassName());
                                        System.out.println(loc[2]);
                                        line = Integer.parseInt(loc[2]);
                                        chosen = pro;
                                    }
                                }
                            } else {
                                File location = new File(loc[0]);
                                for (Program pro : console.getConsole().getProject().getPrograms()) {
                                    if (pro.getFile().toAbsolutePath().toString().equals(location.getAbsolutePath())) {
                                        System.out.println(pro.getClassName());
                                        System.out.println(loc[1]);
                                        line = Integer.parseInt(loc[1]);
                                        chosen = pro;
                                    }
                                }
                            }
                            if (chosen != null) {
                                Parent parent = getTabPane().getParent().getParent();
                                Writer w = (Writer) parent;
                                w.addEditorFromStackTrace(chosen, line);
                            }
                        }
                    }
                } catch (Exception fde) {

                }
            }
        });

        area.setStyle("-fx-font-size:" + fontSize.get().getSize() + ";");
        Writer.fontSize.addListener((ob, older, newer) -> {
            area.setStyle("-fx-font-size:" + newer.getSize() + ";");
        });
        Writer.wrapText.addListener((ob, older, neweer) -> {
            area.setWrapText(neweer);
        });

        append(console.getConsole().getContent());

        console.getConsole().addConsoleListener(new Console.ConsoleListener() {

            @Override
            public void charAdded(char c) {
                addToQueue(c);
            }

            @Override
            public void stringAdded(String c) {
                addToQueue(c);
            }

            @Override
            public void error(String s) {
                addError(s);
            }
        });

        bindKeyListeners();

        center.setBottom(bottom = new BorderPane());
        bottom.setPadding(new Insets(5, 10, 5, 10));
        bottom.setLeft(cancel = new Button("Cancel Process"));
        (timer = new TextTimer()).start();
        cancel.setOnAction((e) -> {
            if (console.getProcess().isAlive()) {
                Alert al = new Alert(AlertType.CONFIRMATION);
                al.setTitle("End Process");
                al.setHeaderText("Are you sure you want to end the process?");
                al.initOwner(getTabPane().getScene().getWindow());
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                        console.getConsole().log("Build Deliberately Stopped");
                        console.cancel();
                        cancel.setDisable(true);
                        if (timer.isAlive()) {
                            timer.stop();
                        }
                    }
                }
            } else {
                cancel.setDisable(true);
                if (timer.isAlive()) {
                    timer.stop();
                }
            }
        });
        setOnCloseRequest((e) -> {
            if (console.getProcess().isAlive()) {
                Alert al = new Alert(AlertType.CONFIRMATION);
                al.setTitle("End Process");
                al.setHeaderText("Are you sure you want to end the process?");
                al.initOwner(getTabPane().getScene().getWindow());
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                        console.getConsole().log("Build Deliberately Stopped");
                        console.cancel();
                        cancel.setDisable(true);
                        if (timer.isAlive()) {
                            timer.stop();
                        }
                    } else {
                        e.consume();
                    }
                } else {
                    e.consume();
                }
            } else {
                if (timer.isAlive()) {
                    timer.stop();
                }
            }
        });
        errorOutput = new TreeMap<>();
        stackTraces = new TreeMap<>();
        fullError = new TreeMap<>();
        fullStack = new TreeMap<>();
    }

    public static String getEntryPath(String className) {
        while (className.contains(".")) {
            String one = className.substring(0, className.indexOf('.'));
            String two = className.substring(className.indexOf('.') + 1);
            className = one + '/' + two;
        }
        return className;
    }

    private int getRow(int caret) {
        String spl[] = area.getText().split("\n");
        int count = 0;
        for (int x = 0; x < spl.length; x++) {
            count += spl[x].length() + 1;
            if (caret <= count) {
                return x;
            }
        }
        return -1;
    }

    private void addToQueue(char c) {
        currentText += c;
    }

    private void addToQueue(String c) {
        currentText += c;
    }

    private void addError(String s) {
        String spl[] = s.split(":");
        if (spl.length > 1) {
            List<String> al = Arrays.asList(spl);
            for (int x = 0; x < al.size(); x++) {
                al.set(x, al.get(x).trim());
            }
            if (al.get(0).length() >= 2) {
                if (al.get(0).substring(0, 2).equals("at")) {
                    addStackTrace(s);
                } else {
                    addErrorOutput(s);
                }
            } else if (al.get(0).length() >= 1) {
                String OS = System.getProperty("os.name").toLowerCase();
                if (al.get(0).charAt(0) == 'C' && OS.contains("win")) {
                    addStackTrace(s);
                } else if (!OS.contains("win") && al.get(0).charAt(0) != 'C') {
                    addStackTrace(s);
                } else {
                    addErrorOutput(s);
                }
            } else {
                addErrorOutput(s);
            }
        } else {
            addErrorOutput(s);
        }
    }

    private void addErrorOutput(String s) {
        errorOutput.put(area.getText().length() + currentText.length(),
                area.getText().length() + currentText.length() + s.length());
        currentText += s;
    }

    private void addStackTrace(String s) {
        if (s.trim().startsWith("at")) {
            int index = s.indexOf("at");
            if (index == -1) {
                index = 0;
            } else {
                int temp = s.substring(index).indexOf("(");
                if (temp == -1) {
                    temp = 0;
                } else {
                    errorOutput.put(area.getText().length() + currentText.length() + index, area.getText().length() + currentText.length() + index + temp + 1);
                    errorOutput.put(area.getText().length() + currentText.length() + s.length() - 1, area.getText().length() + currentText.length() + s.length());
                }
                index += temp;
            }
            stackTraces.put(area.getText().length() + currentText.length() + index + 1, area.getText().length() + currentText.length() + s.length() - 1);
        } else {
            stackTraces.put(area.getText().length() + currentText.length(),
                    area.getText().length() + currentText.length() + s.length());
        }
        currentText += s;
    }

    private class TextTimer extends AnimationTimer {

        private boolean isAlive;

        public TextTimer() {
        }

        @Override
        public void start() {
            super.start();
            isAlive = true;
        }

        @Override
        public void stop() {
            super.stop();
            isAlive = false;
            console.getConsole().cancel();
        }

        public boolean isAlive() {
            return isAlive;
        }

        @Override
        public void handle(long now) {
            if (!currentText.isEmpty()) {
                append(currentText);
                currentText = "";
            }
            Iterator<Map.Entry<Integer, Integer>> iterator = errorOutput.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = iterator.next();
                if (area.getLength() >= entry.getValue()) {
                    area.setStyle(entry.getKey(), entry.getValue(), FXCollections.observableArrayList("red"));
                }
            }
            fullError.putAll(errorOutput);
            errorOutput.clear();
            iterator = stackTraces.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = iterator.next();
                if (area.getLength() >= entry.getValue()) {
                    area.setStyle(entry.getKey(), entry.getValue(), FXCollections.observableArrayList("red",
                            "stacktrace"));
                }
            }
            fullStack.putAll(stackTraces);
            stackTraces.clear();
        }

    }

    public void complete(String process) {
        Platform.runLater(() -> {
            append("\n" + process + " Complete\n");
        });
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
        int caret = area.getCaretPosition();
        if (ke.getCode() == KeyCode.ENTER) {
            if (caret >= area.getLength()) {
                if (console.getProcess().isAlive()) {
                    String s = st.substring(length == 0 ? 0 : (length));
                    printer.println(s);
                    printer.flush();
                    length = area.getText().length();
                }
            } else {
                ke.consume();
            }
        }
        if (ke.getCode() == KeyCode.BACK_SPACE) {
            if (caret <= length) {
                ke.consume();
            }
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
        if (!ke.isAltDown() && !ke.isControlDown() && !ke.isMetaDown() && !ke.isShiftDown()
                && !ke.isShortcutDown()) {
            if (!ke.getText().isEmpty()) {
                int counter = 0;
                for (Paragraph<Collection<String>> pcs : area.getParagraphs()) {
                    counter += pcs.toString().length() + 1;
                }
                counter -= (area.getParagraph(area.getParagraphs().size() - 1).toString().length() + 1);
                if (caret < counter) {
                    String s = area.getText();
                    area.clear();
                    area.appendText(s);
                    style();
                }
            }
        }
    }

    private void style() {
        Iterator<Map.Entry<Integer, Integer>> iterator = fullError.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (area.getLength() >= entry.getValue()) {
                area.setStyle(entry.getKey(), entry.getValue(), FXCollections.observableArrayList("red"));
            }
        }
        iterator = fullStack.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (area.getLength() >= entry.getValue()) {
                area.setStyle(entry.getKey(), entry.getValue(), FXCollections.observableArrayList("red",
                        "stacktrace"));
            }
        }
    }
}
