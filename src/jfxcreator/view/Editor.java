/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxcreator.JFxCreator;
import jfxcreator.core.Highlighter;
import jfxcreator.core.Program;
import jfxcreator.core.Project;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 *
 * @author Aniket
 */
public class Editor extends EnvironmentTab {

    private final CodeArea area;

    public Editor(Program sc, Project pro) {
        super(sc, pro);
        area = new CodeArea();
        area.setContextMenu(new ContextMenu());
        bindMouseListeners();
        area.setFont(Writer.fontSize.get());
        Writer.fontSize.addListener((ob, older, newer) -> {
            area.setFont(newer);
        });
        Writer.wrapText.addListener((ob, older, neweer) -> {
            area.setWrapText(neweer);
        });

        getCenter().setCenter(area);
        readFromScript();
        setOnCloseRequest((e) -> {
            if (canSave()) {
                Alert al = new Alert(Alert.AlertType.CONFIRMATION);
                al.setHeaderText("Would you like to save before closing?");
                al.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.NO);
                al.initOwner(getTabPane().getScene().getWindow());
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                        save();
                    } else if (show.get() == ButtonType.CANCEL) {
                        e.consume();
                    }
                }
            }
        });
        area.getContextMenu().getItems().addAll(
                new MenuItem("Undo"),
                new MenuItem("Redo"),
                new MenuItem("Cut"),
                new MenuItem("Copy"),
                new MenuItem("Paste"));
        area.getContextMenu().getItems().get(0).setOnAction((e) -> {
            area.undo();
        });
        area.getContextMenu().getItems().get(1).setOnAction((e) -> {
            area.redo();
        });
        area.getContextMenu().getItems().get(2).setOnAction((e) -> {
            area.cut();
        });
        area.getContextMenu().getItems().get(3).setOnAction((e) -> {
            area.copy();
        });
        area.getContextMenu().getItems().get(4).setOnAction((e) -> {
            area.paste();
        });
        area.getContextMenu().getItems().stream().forEach((mi) -> {
            mi.setStyle("-fx-font-size:" + Writer.fontSize.get().getSize());
        });
        Writer.fontSize.addListener((ob, older, newer) -> {
            area.getContextMenu().getItems().stream().forEach((mi) -> {
                mi.setStyle("-fx-font-size:" + newer.getSize());
            });
        });
    }

    private void bindMouseListeners() {
//        if (getScript().getType() == Script.JAVASCRIPT) {
//            new JavaScriptHighlighter(area, this);
//        } else if (getScript().getType() == Script.HTML) {
//            new HtmlHighlighter(area, this);
//        } else {
//            area.setParagraphGraphicFactory(LineNumberFactory.get(area));
//        }
        Highlighter.highlight(area, this);
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                area.deleteText(area.getSelection());
                int n = area.getCaretPosition();
                if (n != 0) {
                    String spl[] = area.getText().split("\n");
                    int count = 0;
                    for (String spl1 : spl) {
                        count += spl1.length() + 1;
                        if (n <= count) {
                            String tabs = "\n" + getTabText(spl1);
                            area.insertText(n, tabs);
                            area.positionCaret(n + tabs.length());
                            e.consume();
                            break;
                        }
                    }
                }
            }
            if (e.getCode() == KeyCode.TAB) {
                int n = area.getCaretPosition();
                String spl[] = area.getText().split("\n");
                int count = 0;
                for (String spl1 : spl) {
                    count += spl1.length() + 1;
                    if (n <= count) {
                        String b = area.getText().substring(n);
                        area.insertText(n, "    ");
                        area.positionCaret(n + 4);
                        e.consume();
                        break;
                    }
                }
            }
            if (e.isControlDown() && e.getCode() == KeyCode.F) {
                HBox box = new HBox(15);
                BorderPane main = new BorderPane(box);
                box.setPadding(new Insets(5, 10, 5, 10));
                box.setStyle("-fx-background-fill:gray;");
                TextField fi;
                Button prev, next;
                box.getChildren().addAll(fi = new TextField(),
                        prev = new Button("Previous"),
                        next = new Button("Next"));
                fi.setOnAction((ea) -> {
                    if (area.getSelection().getLength() == 0) {
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
                    index += end;
                    if (index != -1) {
                        area.selectRange(index, index + fi.getText().length());
                    }
                });
                Button close;
                main.setRight(close = new Button("X"));
                BorderPane.setMargin(main.getRight(), new Insets(5, 10, 5, 10));
                getCenter().setBottom(main);
                fi.requestFocus();
                close.setOnAction((se) -> {
                    getCenter().setBottom(null);
                });
            }
            if (e.isControlDown() && e.getCode() == KeyCode.H) {
                VBox total = new VBox();
                total.setStyle("-fx-background-fill:gray;");
                BorderPane main = new BorderPane(total);
                HBox top = new HBox(15);
                HBox bottom = new HBox(5);
                top.setStyle("-fx-background-fill:gray;");
                bottom.setStyle("-fx-background-fill:gray;");
                total.getChildren().addAll(top, bottom);
                bottom.setPadding(new Insets(5, 10, 5, 10));
                top.setPadding(new Insets(5, 10, 5, 10));
                TextField fi, replace;
                Button prev, next, rep, reAll, close;
                top.getChildren().addAll(fi = new TextField(),
                        prev = new Button("Previous"),
                        next = new Button("Next"));
                bottom.getChildren().addAll(replace = new TextField(),
                        rep = new Button("Replace"),
                        reAll = new Button("Replace All"));
                fi.setOnAction((ea) -> {
                    if (area.getSelection().getLength() == 0) {
                        String a = fi.getText();
                        int index = area.getText().indexOf(a);
                        if (index != -1) {
                            area.selectRange(index, index + a.length());
                        }
                    } else {
                        next.fire();
                    }
                });
                replace.setOnAction((es) -> {
                    rep.fire();
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
                    index += end;
                    if (index != -1) {
                        area.selectRange(index, index + fi.getText().length());
                    }
                });
                rep.setOnAction((sdfsdfsd) -> {
                    String a = fi.getText();
                    String b = replace.getText();
                    if (area.getText().contains(a)) {
                        int index = area.getText().indexOf(a);
                        area.replaceText(index, index + a.length(), b);
                    }
                });
                reAll.setOnAction((efsf) -> {
                    String a = fi.getText();
                    String b = replace.getText();
                    while (area.getText().contains(a)) {
                        int index = area.getText().indexOf(a);
                        area.replaceText(index, index + a.length(), b);
                    }
                });
                main.setRight(close = new Button("X"));
                BorderPane.setMargin(main.getRight(), new Insets(5, 10, 5, 10));
                getCenter().setBottom(main);
                fi.requestFocus();
                close.setOnAction((se) -> {
                    getCenter().setBottom(null);
                });
            }
        });
    }

    private String getTabText(String s) {
        int count = 0;
        for (int x = 0; x < s.length(); x += 4) {
            if (s.length() > x + 3) {
                if (s.substring(x, x + 4).equals("    ")) {
                    count++;
                }
            } else {
                break;
            }
        }
        String ret = "";
        String temp = s.trim();
        if (temp.endsWith(")") || temp.endsWith("{")) {
            count++;
        }
        for (int x = 0; x < count; x++) {
            ret += "    ";
        }
        return ret;
    }

    private void readFromScript() {
        List<String> read = getScript().getLastCode();
        read.stream().forEach((s) -> {
            area.appendText(s + "\n");
        });
    }

    public CodeArea getCodeArea() {
        return area;
    }

    public void save() {
        List<String> asList = Arrays.asList(area.getText().split("\n"));
        if (getScript().canSave(asList)) {
            getScript().save(asList);
        }
    }

    public boolean canSave() {
        List<String> asList = Arrays.asList(area.getText().split("\n"));
        return getScript().canSave(asList);
    }

    public void undo() {
        area.undo();
    }

    public void redo() {
        area.redo();
    }

    public void cut() {
        area.cut();
    }

    public void copy() {
        area.copy();
    }

    public void paste() {
        area.paste();
    }

    public void selectAll() {
        area.selectAll();
    }

}
