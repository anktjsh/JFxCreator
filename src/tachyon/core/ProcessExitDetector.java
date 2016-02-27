/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aniket
 */
public class ProcessExitDetector extends Thread {

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
            int val = process.getProcess().exitValue();
            listeners.stream().forEach((listener) -> {
                listener.processFinished(process.getProcess(), val);
            });
            return;
        } catch (IllegalThreadStateException eg) {
        }
        try {
            int val = process.getProcess().waitFor();
            listeners.stream().forEach((listener) -> {
                listener.processFinished(process.getProcess(), val);
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
