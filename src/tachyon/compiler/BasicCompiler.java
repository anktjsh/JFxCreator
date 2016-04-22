/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.compiler;

import java.util.Locale;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import tachyon.core.JavaProgram;
import tachyon.core.Program;
import tachyon.view.Editor;

/**
 *
 * @author Aniket
 */
public class BasicCompiler extends Compiler {

    private final Program program;
    private final Editor edit;

    public BasicCompiler(Editor pro) {
        super(pro.getProject());
        program = pro.getScript();
        edit = pro;
    }

    @Override
    public void prepare() {
        if (program instanceof JavaProgram) {
            ObservableList<DynamicJavaSourceCodeObject> objs = FXCollections.observableArrayList();
            objs.add(new DynamicJavaSourceCodeObject(((JavaProgram) program).getClassName(), edit.getCodeArea().getText()));
            JavaCompiler.CompilationTask task = getCompiler().getTask(null, getFileManager(), getDiagnosticCollector(), getCompilerOptions(), null, objs);
            TreeMap<Long, String> map = new TreeMap<>();
            boolean status = task.call();
            if (!status) {
                for (Diagnostic c : getDiagnosticCollector().getDiagnostics()) {
                    map.put(c.getLineNumber() - 1, c.getMessage(Locale.getDefault()));
                }
            }
            ((JavaProgram) program).hasErrors(map);
            recreateFileManager();
            getCompilerOptions().clear();
        }
    }
}
