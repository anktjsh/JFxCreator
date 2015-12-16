/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import jfxcreator.core.ProcessPool.ProcessItem;

/**
 *
 * @author Aniket
 */
public class ConsoleWindow extends Tab {
    
    private final ProcessItem console;
    private final TextArea area;
    
    public ConsoleWindow(ProcessItem c) {
        super(c.getName());
        console = c;
        area = new TextArea();
        setContent(area);
        appendAll(console.getConsole().getList());
        console.getConsole().getList().addListener((ListChangeListener.Change<? extends String> c1) -> {
            c1.next();
            if (c1.wasAdded()) {
                for (String s : c1.getAddedSubList()) {
                    append(s);
                }
            }
        });
        bindKeyListeners();
    }
    
    private void appendAll(List<String> al) {
        for (String s : al) {
            append(s);
        }
    }
    
    private void append(String s) {
        if (Platform.isFxApplicationThread()) {
            area.appendText(s + "\n");
        } else {
            Platform.runLater(() -> {
                area.appendText(s + "\n");
            });
        }
    }
    
    private void bindKeyListeners() {
        area.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                
            }
            if (e.getCode() == KeyCode.BACK_SPACE) {
                
            }
            if (e.getCode() == KeyCode.F) {
                if (e.isControlDown()) {
                    
                }
            }
        });
    }
}
