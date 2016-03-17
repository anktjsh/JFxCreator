/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tachyon.core.Program;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class EnvironmentTab extends Tab {

    private final BorderPane content;
    private final Project project;
    private final Program script;
    private final Label graph;

    public EnvironmentTab(Program scr, Project pro) {
        setGraphic(graph = new Label(scr == null ? "" : scr.getFile().getFileName().toString()));
        project = pro;
        content = new BorderPane();
        setContent(content);
        script = scr;
        setContextMenu(new ContextMenu());
        getContextMenu().getItems().addAll(new MenuItem("Close"),
                new MenuItem("Close All"),
                new MenuItem("Copy File"),
                new MenuItem("Copy File Path"),
                new MenuItem("File Details"),
                new MenuItem("Word Count"));
        getContextMenu().getItems().get(0).setOnAction((e) -> {
            close();
        });
        getContextMenu().getItems().get(1).setOnAction((e) -> {
            if (getTabPane() != null) {
                ObservableList<Tab> tabs = getTabPane().getTabs();
                for (int x = tabs.size() - 1; x >= 0; x--) {
                    Tab b = tabs.get(x);
                    if (b instanceof EnvironmentTab) {
                        ((EnvironmentTab) b).close();
                    }
                }
            }
        });
        getContextMenu().getItems().get(2).setOnAction((e) -> {
            if (script != null) {
                Clipboard cb = Clipboard.getSystemClipboard();
                ClipboardContent cc = new ClipboardContent();
                cc.putString(script.getFile().toAbsolutePath().toString());
                cc.putUrl(script.getFile().toUri().toString());
                cc.putFiles(FXCollections.observableArrayList(script.getFile().toFile()));
                if (this instanceof Viewer) {
                    cc.putImage(((Viewer) this).getImage());
                }
            }
        });
        getContextMenu().getItems().get(3).setOnAction((e) -> {
            if (script != null) {
                Clipboard cb = Clipboard.getSystemClipboard();
                ClipboardContent cc = new ClipboardContent();
                cc.putString(script.getFile().toAbsolutePath().toString());
                cc.putUrl(script.getFile().toUri().toString());
                cb.setContent(cc);
            }
        });
        getContextMenu().getItems().get(4).setOnAction((e) -> {
            if (script != null) {
                Details.getDetails((Stage) getTabPane().getScene().getWindow(), script.getFile()).showAndWait();
            }
        });
        getContextMenu().getItems().get(5).setOnAction((e) -> {
            if (script != null) {
                WordCount.getWordCount((Stage) getTabPane().getScene().getWindow(), script);
            }
        });

    }

    public final void close() {
        if (Platform.isFxApplicationThread()) {
            Event.fireEvent(this, new Event(Tab.TAB_CLOSE_REQUEST_EVENT));
            if (getTabPane() != null) {
                getTabPane().getTabs().remove(this);
            }
        } else {
            Platform.runLater(() -> {
                Event.fireEvent(this, new Event(Tab.TAB_CLOSE_REQUEST_EVENT));
                if (getTabPane() != null) {
                    getTabPane().getTabs().remove(this);
                }
            });
        }
    }

    public Label getGraph() {
        return graph;
    }

    public BorderPane getCenter() {
        return content;
    }

    public Program getScript() {
        return script;
    }

    public Project getProject() {
        return project;
    }
}
