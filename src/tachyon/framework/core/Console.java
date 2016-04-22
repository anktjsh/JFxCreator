/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.framework.core;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import tachyon.view.ConsoleWindow;

/**
 *
 * @author Aniket
 */
public class Console {

    private String content;
    private final Project proje;
    private final ObjectProperty<ConsoleWindow> window;
    private final ArrayList<ConsoleListener> listen;

    public Console(Project project) {
        proje = project;
        content = "";
        window = new SimpleObjectProperty<>();
        listen = new ArrayList<>();
    }

    public void setConsoleWindow(ConsoleWindow cs) {
        window.set(cs);
    }

    public ConsoleWindow getConsoleWindow() {
        return window.get();
    }

    public Project getProject() {
        return proje;
    }

    public void log(String st) {
        content += st;
        for (ConsoleListener cl : listen) {
            cl.stringAdded(st);
        }
        cancel();
    }

    public void log(char c) {
        content += c;
        for (ConsoleListener cl : listen) {
            cl.charAdded(c);
        }
        cancel();
    }

    public void error(String s) {
        content += s;
        for (ConsoleListener cl : listen) {
            cl.error(s);
        }
        cancel();
    }

    public void complete(String s) {
        if (window != null) {
            if (s.startsWith("Launching")) {
                (new Thread(() -> {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException ei) {
                    }
                    Platform.runLater(() -> {
                        window.get().complete(s);
                    });
                })).start();
            } else {
                window.get().complete(s);
            }
        }
    }

    public String getContent() {
        return content;
    }

    public void cancel() {
        content = "";
    }

    public void merge(Console col) {
        addConsoleListener(new ConsoleListener() {

            @Override
            public void charAdded(char c) {
                col.log(c);
            }

            @Override
            public void stringAdded(String c) {
                col.log(c);
            }

            @Override
            public void error(String s) {
                col.error(s);
            }

        });
    }

    public void addConsoleListener(ConsoleListener listn) {
        listen.add(listn);
    }

    public interface ConsoleListener {

        public void charAdded(char c);

        public void stringAdded(String c);

        public void error(String s);
    }
}
