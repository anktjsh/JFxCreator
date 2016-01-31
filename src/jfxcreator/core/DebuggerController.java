/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.OutputStream;
import java.io.PrintStream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Aniket
 */
public class DebuggerController {
    
    private final ObjectProperty<PrintStream> out;
    private final BooleanProperty available;
    
    public DebuggerController() {
        out = new SimpleObjectProperty<>();
        available = new SimpleBooleanProperty();
    }
    
    public boolean isAvailable() {
        return available.get();
    }
    
    public void process(String s) {
        out.get().println(s);
        out.get().flush();
    }
    
    public ObjectProperty<PrintStream> outputProperty() {
        return out;
    }
    
    public void finished() {
        available.set(false);
        out.set(null);
    }
    
    public void setOutputStream(OutputStream oos) {
        available.set(true);
        out.set(new PrintStream(oos));
    }
    
}
