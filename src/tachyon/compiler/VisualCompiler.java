/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import tachyon.core.JavaProgram;
import tachyon.core.Program;
import tachyon.view.Editor;

/**
 *
 * @author Aniket
 */
public class VisualCompiler extends Compiler {

    private final ObservableList<Editor> allEditors;
    private final ObservableList<JavaProgram> allPrograms;

    public VisualCompiler(Editor edit) {
        super(edit.getProject());
        allEditors = FXCollections.observableArrayList();
        allPrograms = FXCollections.observableArrayList();
        ArrayList<JavaProgram> al = new ArrayList<>();
        for (Tab b : edit.getTabPane().getTabs()) {
            if (b instanceof Editor) {
                Editor ed = (Editor) b;
                if (ed.getScript() instanceof JavaProgram) {
                    if (!al.contains((JavaProgram) ed.getScript())) {
                        al.add((JavaProgram) ed.getScript());
                        allEditors.add(ed);
                    }
                }
            }
        }
        for (Program p : getProject().getPrograms()) {
            if (p instanceof JavaProgram) {
                if (!al.contains((JavaProgram) p)) {
                    allPrograms.add((JavaProgram) p);
                }
            }
        }
    }

    public void prepare() {
        ObservableList<DynamicJavaSourceCodeObject> objs = FXCollections.observableArrayList();
        for (Editor ed : allEditors) {
            objs.add(new DynamicJavaSourceCodeObject(((JavaProgram) ed.getScript()).getClassName(), ed.getCodeArea().getText()));
        }
        for (JavaProgram p : allPrograms) {
            objs.add(new DynamicJavaSourceCodeObject(p.getClassName(), p.getLastCode()));
        }
//        System.out.println(allPrograms.size());
//        System.out.println(objs.size());
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
        ArrayList<JavaProgram> sent = new ArrayList<>();
        for (String key : map.keySet()) {
            JavaProgram p = getProgram(key);
            if (p != null) {
                sent.add(p);
                p.hasErrors(map.get(key));
            }
        }
        for (Program p : getProject().getPrograms()) {
            if (p instanceof JavaProgram) {
                if (!sent.contains((JavaProgram) p)) {
                    ((JavaProgram) p).hasErrors(new TreeMap<>());
                }
            }
        }
        recreateFileManager();
        getCompilerOptions().clear();
    }
}
