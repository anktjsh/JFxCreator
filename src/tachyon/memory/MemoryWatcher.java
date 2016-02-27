/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.memory;

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

    private final LongProperty freeMemory, usedMemory, totalMemory, maxMemory;
    private final ObservableList<Pair<LocalTime, Long>> previousFree, previousUsed, previousTotal;
    private final ObservableList<ThreadStatus> status;
    private boolean running;

    public MemoryWatcher() {
        freeMemory = new SimpleLongProperty(Runtime.getRuntime().freeMemory());
        usedMemory = new SimpleLongProperty(Runtime.getRuntime().totalMemory() - freeMemory.get());
        totalMemory = new SimpleLongProperty(Runtime.getRuntime().totalMemory());
        maxMemory = new SimpleLongProperty(Runtime.getRuntime().maxMemory());
        previousFree = FXCollections.observableArrayList();
        previousUsed = FXCollections.observableArrayList();
        previousTotal = FXCollections.observableArrayList();
        status = FXCollections.observableArrayList();
        running = false;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cancel();
        }));
    }

    ObservableList<ThreadStatus> getStatusList() {
        return status;
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

    public LongProperty maxMemoryProperty() {
        return maxMemory;
    }

    public long getFreeMemory() {
        return freeMemory.get();
    }

    public long getUsedMemory() {
        return usedMemory.get();
    }

    public long getTotalMemory() {
        return totalMemory.get();
    }

    public long getMaxMemory() {
        return maxMemory.get();
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
            waitOneSecond();
            checkThreads();
            waitOneSecond();
            checkThreads();
            waitOneSecond();
            checkThreads();
            waitOneSecond();
            checkThreads();
            waitOneSecond();
            long freeMemory1 = Runtime.getRuntime().freeMemory();
            long usedMemory1 = Runtime.getRuntime().totalMemory() - freeMemory1;
            long totalMemory1 = Runtime.getRuntime().totalMemory();
            long maxMemory1 = Runtime.getRuntime().maxMemory();
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
            if (maxMemory1 != maxMemory.get()) {
                maxMemory.set(maxMemory1);
            }
            checkThreads();
        }
    }

    private void waitOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void checkThreads() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (!isStatusContainsThread(t)) {
                status.add(new ThreadStatus(t));
            }
        }
    }

    private boolean isStatusContainsThread(Thread t) {
        for (ThreadStatus ts : status) {
            if (ts.getThread().equals(t)) {
                return true;
            }
        }
        return false;
    }

    class ThreadStatus {

        private final Thread thr;

        public ThreadStatus(Thread th) {
            thr = th;
        }

        public Thread getThread() {
            return thr;
        }

        @Override
        public String toString() {
            return thr.getName() + " : " + (thr.isAlive() ? "Alive" : "Terminated");
        }
    }

}
