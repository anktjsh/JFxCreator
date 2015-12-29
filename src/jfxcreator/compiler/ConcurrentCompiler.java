/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.compiler;

import java.io.File;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import jfxcreator.view.Editor;

/**
 *
 * @author Aniket
 */
public class ConcurrentCompiler {

    private static ConcurrentCompiler instance;

    public static ConcurrentCompiler getInstance() {
        if (instance == null) {
            instance = new ConcurrentCompiler();
        }
        return instance;
    }

    private final BooleanProperty allowed;
    private final ObjectProperty<Editor> lastCall;

    private ConcurrentCompiler() {
        allowed = new SimpleBooleanProperty(false);
        runners = new ArrayList<>();
        lastCall = new SimpleObjectProperty<>();
    }

    public synchronized void compile(Editor edit) {
        lastCall.set(edit);
        if (allowed.get()) {
            System.out.println("Preparing on " + Thread.currentThread().getName());
            allowed.set(false);
            Compiler comp = new Compiler(edit);
            comp.setDirectory(new File(".cache" + File.separator + edit.getProject().getProjectName() + File.separator + "builds"));
            if (edit.getProject().getNumLibs() != 0) {
                comp.addToClassPath(edit.getProject().getAllLibs());
            }
            comp.prepare();
            lastCall.set(null);
        }
        (new Thread(new Runner())).start();
    }
    private static ArrayList<Runner> runners;

    private class Runner implements Runnable {

        public Runner() {
            runners.add(this);
        }

        @Override
        public void run() {
            int x = runners.size();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            if (x == runners.size()) {
                allowed.set(true);
                if (lastCall.get() != null) {
                    compile(lastCall.get());
                }
            }

        }

    }
}
