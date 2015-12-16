/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

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
        ped.addProcessListener((Process process) -> {
            for (int x = items.size() - 1; x >= 0; x--) {
                if (items.get(x).getProcess().equals(process)) {
                    pi.getConsole().log("Process Terminated : " + items.get(x).getName());
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

        private final String name;
        private final Process proc;
        private final Console console;

        public ProcessItem(String name, Process proc, Console con) {
            this.name = name;
            this.proc = proc;
            console = con;
        }

        public String getName() {
            return name;
        }

        public Process getProcess() {
            return proc;
        }

        public Console getConsole() {
            return console;
        }

        public ProcessItem merge(ProcessItem pti) {
            console.merge(pti.getConsole());
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
                process.getConsole().log("Waiting for Process to conclude");
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
