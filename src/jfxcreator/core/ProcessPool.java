/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Aniket
 */
public class ProcessPool {

    private static ProcessPool pool;

    public static ProcessPool getPool() {
        if (pool == null) {
            pool = new ProcessPool();
        }
        return pool;
    }
    private final ArrayList<ProcessItem> items;

    private ProcessPool() {
        items = new ArrayList<>();
    }

    public void addItem(ProcessItem pi) {
        items.add(pi);
        ProcessExitDetector ped = new ProcessExitDetector(pi);
        ped.addProcessListener((process) -> {
            for (int x = items.size() - 1; x >= 0; x--) {
                if (items.get(x).getProcess().equals(process)) {
                    pi.getConsole().complete(pi.getName());
                    items.remove(x);
                    break;
                }
            }
        });
        ped.start();
    }

    public String getAccumulatedText() {
        String s = "";
        s = items.stream().map((pt) -> pt.getName() + "\n").reduce(s, String::concat);
        return s;
    }

    public int size() {
        return items.size();
    }

    public void cancel() {
        items.stream().forEach((pt) -> {
            pt.getProcess().destroy();
        });
    }

    public static class ProcessItem {

        private final ObjectProperty<String> nameProperty;
        private final ObjectProperty<Process> processProperty;
        private final ObjectProperty<Console> consoleProperty;

        public ProcessItem(String name, Process proc, Console con) {
            nameProperty = new SimpleObjectProperty<>(name);
            processProperty = new SimpleObjectProperty<>(proc);
            consoleProperty = new SimpleObjectProperty<>(con);
        }

        public ObjectProperty<Console> consoleProperty() {
            return consoleProperty;
        }

        public ObjectProperty<Process> processProperty() {
            return processProperty;
        }

        public ObjectProperty<String> nameProperty() {
            return nameProperty;
        }

        public String getName() {
            return nameProperty.get();
        }

        public Process getProcess() {
            return processProperty.get();
        }

        public Console getConsole() {
            return consoleProperty.get();
        }

        public void setProcess(Process con) {
            processProperty.set(con);
        }

        public void setName(String str) {
            nameProperty.set(str);
        }

        public ProcessItem merge(ProcessItem pti) {
            getConsole().merge(pti.getConsole());
            return pti;
        }

    }

    private interface ProcessListener extends EventListener {

        void processFinished(Process process);
    }

    private class ProcessExitDetector extends Thread {

        private final ProcessItem process;
        private final List<ProcessListener> listeners = new ArrayList<>();

        public ProcessExitDetector(ProcessItem process) {
            this.process = process;
        }

        public ProcessItem getProcess() {
            return process;
        }

        @Override
        public void run() {
            try {
                process.getProcess().exitValue();
                listeners.stream().forEach((listener) -> {
                    listener.processFinished(process.getProcess());
                });
                return;
            } catch (IllegalThreadStateException eg) {
            }
            try {
                //process.getConsole().log("\nWaiting for Process to conclude\n");
                process.getProcess().waitFor();
                listeners.stream().forEach((listener) -> {
                    listener.processFinished(process.getProcess());
                });
            } catch (InterruptedException e) {
            }
        }

        /**
         * Adds a process listener.
         *
         * @param listener the listener to be added
         */
        public void addProcessListener(ProcessListener listener) {
            listeners.add(listener);
        }

        /**
         * Removes a process listener.
         *
         * @param listener the listener to be removed
         */
        public void removeProcessListener(ProcessListener listener) {
            listeners.remove(listener);
        }
    }
}
