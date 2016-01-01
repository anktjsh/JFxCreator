/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import jfxcreator.view.ConsoleWindow;

/**
 *
 * @author Aniket
 */
public class Console {

    private final ObservableList<Character> list;
    private final Project proje;
    private final ObjectProperty<ConsoleWindow> window;

    public Console(Project project) {
        proje = project;
        list = FXCollections.observableArrayList();
        window = new SimpleObjectProperty<>();
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

    public ObservableList<Character> getList() {
        return list;
    }

    public void log(String st) {
        for (char c : st.toCharArray()) {
            log(c);
        }
    }

    public synchronized void log(char c) {
        list.add(c);
    }

    public void complete(String s) {
        if (window != null) {
            window.get().complete(s);
        }
    }

    public void merge(Console col) {
        col.getList().addAll(0, getList());
        getList().addListener((ListChangeListener.Change<? extends Character> c) -> {
            c.next();
            if (c.wasAdded()) {
                col.getList().addAll(c.getAddedSubList());
            }
        });
    }
}
