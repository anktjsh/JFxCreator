/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.process;

import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import tachyon.framework.core.Console;

/**
 *
 * @author Aniket
 */
public class ProcessItem {

    private final BooleanProperty isCancelled;
    private final ObjectProperty<String> nameProperty;
    private final ObjectProperty<Process> processProperty;
    private final ObjectProperty<Console> consoleProperty;

    public ProcessItem(String name, Process proc, Console con) {
        nameProperty = new SimpleObjectProperty<>(name);
        processProperty = new SimpleObjectProperty<>(proc);
        consoleProperty = new SimpleObjectProperty<>(con);
        isCancelled = new SimpleBooleanProperty(false);
    }

    public BooleanProperty isCancelledProperty() {
        return isCancelled;
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

    private void endProcess(Process p) {
        try {
            p.getOutputStream().close();
        } catch (IOException ex) {
        }
        if (p.isAlive()) {
            p.destroyForcibly();
        }
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }

    public void cancel() {
        isCancelled.set(true);
        endProcess(getProcess());
        processProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                endProcess(newer);
            }
        });
    }

}
