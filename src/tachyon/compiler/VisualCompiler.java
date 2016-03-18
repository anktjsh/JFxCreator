/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import tachyon.core.JavaLibrary;
import tachyon.core.Program;
import tachyon.core.Project;
import tachyon.view.Editor;

/**
 *
 * @author Aniket
 */
public class VisualCompiler extends Compiler {

    private final ObservableList<Editor> allEditors;
    private final ObservableList<Program> allPrograms;

    public VisualCompiler(Editor edit) {
        super(edit.getProject());
        allEditors = FXCollections.observableArrayList();
        allPrograms = FXCollections.observableArrayList();
        ArrayList<Program> al = new ArrayList<>();
        for (Tab b : edit.getTabPane().getTabs()) {
            if (b instanceof Editor) {
                Editor ed = (Editor) b;
                if (!al.contains(ed.getScript())) {
                    if (ed.getScript().getType() == Program.JAVA) {
                        al.add(ed.getScript());
                        allEditors.add(ed);
                    }
                }
            }
        }
        for (Program p : getProject().getPrograms()) {
            if (!al.contains(p)) {
                if (p.getType() == Program.JAVA) {
                    allPrograms.add(p);
                }
            }
        }
    }

    public void prepare() {
        ObservableList<DynamicJavaSourceCodeObject> objs = FXCollections.observableArrayList();
        for (Editor ed : allEditors) {
            objs.add(new DynamicJavaSourceCodeObject(ed.getScript().getClassName(), ed.getCodeArea().getText()));
        }
        for (Program p : allPrograms) {
            objs.add(new DynamicJavaSourceCodeObject(p.getClassName(), p.getLastCode()));
        }
        System.out.println(allPrograms.size());
        System.out.println(objs.size());
        JavaCompiler.CompilationTask task = getCompiler().getTask(null, getFileManager(), getDiagnosticCollector(), getCompilerOptions(), null, objs);
        HashMap<String, TreeMap<Long, String>> map = new HashMap<>();
        boolean status = task.call();
        if (!status) {
            for (Diagnostic c : getDiagnosticCollector().getDiagnostics()) {
                if (map.containsKey(c.getSource().toString())) {
                    map.get(c.getSource().toString()).put(c.getLineNumber() - 1, c.getMessage(Locale.getDefault()));
                } else {
                    map.put(c.getSource().toString(), new TreeMap<>());
                    map.get(c.getSource().toString()).put(c.getLineNumber() - 1, c.getMessage(Locale.getDefault()));
                }
            }
        }
        ArrayList<Program> sent = new ArrayList<>();
        for (String key : map.keySet()) {
            Program p = getProgram(key);
            if (p != null) {
                sent.add(p);
                p.hasErrors(map.get(key));
            }
        }
        for (Program p : getProject().getPrograms()) {
            if (!sent.contains(p)) {
                p.hasErrors(new TreeMap<>());
            }
        }
        recreateFileManager();
        getCompilerOptions().clear();
    }
}
