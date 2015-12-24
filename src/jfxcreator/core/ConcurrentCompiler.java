/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;

/**
 *
 * @author swatijoshi
 */
public class ConcurrentCompiler implements Runnable {

    private final Project currentProject;
    private boolean running;
    private final Console con;
    private String temp;
    private final StringProperty output = new SimpleStringProperty();

    public StringProperty outputProperty() {
        return output;
    }

    public ConcurrentCompiler(Project pro) {
        currentProject = pro;
        con = new Console(currentProject);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cancel();
        }));
    }

    private void compile() {
        temp = "";
        con.getList().addListener((ListChangeListener.Change<? extends Character> c) -> {
            c.next();
            if (c.wasAdded()) {
                for (Character ca : c.getAddedSubList()) {
                    temp += ca;
                }
            }
        });
        currentProject.concurrentCompiling(con);
        for (String s : temp.split("\n")) {
            System.out.println(s);
        }
        output.set(temp);
    }

    public void cancel() {
        running = false;
    }

    private boolean reset() {
//        return !currentProject.isModified();
        return true;
    }

    @Override
    public void run() {
        running = false;
        while (running) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            if (reset()) {
                continue;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            if (reset()) {
                continue;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            if (reset()) {
                continue;
            }
            compile();
        }
        compile();
    }

}
