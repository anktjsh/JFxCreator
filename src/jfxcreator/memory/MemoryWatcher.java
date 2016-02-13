/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.memory;

import java.time.LocalTime;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 *
 * @author Aniket
 */
public class MemoryWatcher implements Runnable {

    private final LongProperty freeMemory, usedMemory, totalMemory;
    private final ObservableList<Pair<LocalTime, Long>> previousFree, previousUsed, previousTotal;
    private boolean running;

    public MemoryWatcher() {
        freeMemory = new SimpleLongProperty(Runtime.getRuntime().freeMemory());
        usedMemory = new SimpleLongProperty(Runtime.getRuntime().totalMemory() - freeMemory.get());
        totalMemory = new SimpleLongProperty(Runtime.getRuntime().totalMemory());
        previousFree = FXCollections.observableArrayList();
        previousUsed = FXCollections.observableArrayList();
        previousTotal = FXCollections.observableArrayList();
        running = false;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cancel();
        }));
    }

    public ObservableList<Pair<LocalTime, Long>> freeList() {
        return previousFree;
    }

    public ObservableList<Pair<LocalTime, Long>> usedList() {
        return previousUsed;
    }

    public ObservableList<Pair<LocalTime, Long>> totalList() {
        return previousTotal;
    }

    public LongProperty freeMemoryProperty() {
        return freeMemory;
    }

    public LongProperty usedMemoryProperty() {
        return usedMemory;
    }

    public LongProperty totalMemoryProperty() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory.get();
    }

    public long getMaxMemory() {
        return usedMemory.get();
    }

    public long getTotalMemory() {
        return totalMemory.get();
    }

    private void cancel() {
        running = false;
    }

    private void log(int a, long b) {
        switch (a) {
            case 0:
                previousFree.add(new Pair(LocalTime.now(), b));
                break;
            case 1:
                previousUsed.add(new Pair(LocalTime.now(), b));
                break;
            case 2:
                previousTotal.add(new Pair(LocalTime.now(), b));
                break;
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            long freeMemory1 = Runtime.getRuntime().freeMemory();
            long usedMemory1 = Runtime.getRuntime().totalMemory() - freeMemory1;
            long totalMemory1 = Runtime.getRuntime().totalMemory();
            if (freeMemory1 != freeMemory.get()) {
                freeMemory.set(freeMemory1);
                log(0, freeMemory1);
            }
            if (usedMemory1 != usedMemory.get()) {
                usedMemory.set(usedMemory1);
                log(1, usedMemory1);
            }
            if (totalMemory1 != totalMemory.get()) {
                totalMemory.set(totalMemory1);
                log(2, totalMemory1);
            }
        }
    }

}
