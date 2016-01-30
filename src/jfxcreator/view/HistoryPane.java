/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import jfxcreator.core.Highlighter;
import org.fxmisc.richtext.CodeArea;

/**
 *
 * @author swatijoshi
 */
public class HistoryPane extends BorderPane {

    private final HBox box;
    private final CodeArea code1, code2;
    private final ListView<String> options;
    private final Editor edit;

    public HistoryPane(Editor edi) {
        edit = edi;
        box = new HBox(10);
        setPadding(new Insets(5, 10, 5, 10));
        ScrollPane sc, scr;
        box.setPadding(new Insets(5, 10, 5, 10));
        box.getChildren().addAll(sc = new ScrollPane(code1 = new CodeArea()), scr = new ScrollPane(code2 = new CodeArea()));
        Highlighter.highlight(code1);
        Highlighter.highlight(code2);
        sc.setFitToHeight(true);
        scr.setFitToHeight(true);
        sc.setFitToWidth(true);
        scr.setFitToWidth(true);
        box.setFillHeight(true);
        box.setAlignment(Pos.CENTER);
        code1.setEditable(false);
        code2.setEditable(false);
        setCenter(box);
        box.widthProperty().addListener((ob, older, newer) -> {
            sc.setMinWidth(newer.doubleValue() / 2 - 25);
            scr.setMinWidth(newer.doubleValue() / 2 - 25);
        });
        code1.appendText(edi.getCodeArea().getText());
        options = new ListView<>();
        options.setMaxHeight(200);
        options.getItems().addAll(edi.getScript().previousSavedDates());
        Collections.reverse(options.getItems());
        setTop(options);
        options.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                if (options.getSelectionModel().getSelectedItem() != null) {
                    code2.clear();
                    code2.appendText(getCode(edi.getScript().getPreviousCode(options.getSelectionModel().getSelectedItem())));
                }
            }
        });
    }

    public void refresh() {
        options.getItems().clear();
        options.getItems().addAll(edit.getScript().previousSavedDates());
        Collections.reverse(options.getItems());
        code1.clear();
        code1.appendText(edit.getCodeArea().getText());
    }

    private String getCode(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

}
