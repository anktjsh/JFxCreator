/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.process;

import tachyon.process.ProcessItem;
import tachyon.process.ProcessExitDetector;
import java.util.ArrayList;

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
        ped.addProcessListener((process, exitValue) -> {
            for (int x = items.size() - 1; x >= 0; x--) {
                if (items.get(x).getProcess().equals(process)) {
                    pi.getConsole().complete(pi.getName());
                    if (exitValue != 0) {
                        pi.cancel();
                    }
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
        for (ProcessItem pt : items) {
            pt.getProcess().destroy();
        }
    }

}
