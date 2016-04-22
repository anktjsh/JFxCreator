/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.java.core;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tachyon.framework.core.Program;
import tachyon.framework.core.Project;

/**
 *
 * @author Aniket
 */
public class JavaProgram extends Program {

    private String className;
    private final ObservableList<JavaProgramListener> programs;
    private final BooleanProperty hasErrors;
    private final ObservableList<Long> breaks;
    private final TreeMap<Long, String> errors;
    private final HashMap<String, List<String>> previous = new HashMap<>();

    public JavaProgram(Path p, List<String> cod, Project pro, String name) {
        super(p, cod, pro);
        className = name;
        if (!className.contains(".")) {
            verifyClassName();
        }
        programs = FXCollections.observableArrayList();
        breaks = FXCollections.observableArrayList();
        errors = new TreeMap<>();
        hasErrors = new SimpleBooleanProperty(false);
    }

    public String getClassName() {
        return className;
    }

    private void verifyClassName() {
        if (getProject() != null) {
            String src = getProject().getSource().toAbsolutePath().toString();
            String name = getFile().toAbsolutePath().toString().substring(0, getFile().toAbsolutePath().toString().lastIndexOf(".java"));
            name = name.substring(src.length() + 1);
            className = getClassName(name);
        }
    }

    public static String getClassName(String filePath) {
        if (filePath.charAt(0) == File.separatorChar) {
            filePath = filePath.substring(1);
        }
        if (filePath.charAt(filePath.length() - 1) == File.separatorChar) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }
        while (filePath.contains(File.separator)) {
            String one = filePath.substring(0, filePath.indexOf(File.separator));
            String two = filePath.substring(filePath.indexOf(File.separator) + 1);
            filePath = one + "." + two;
        }
        return filePath;
    }

    public void addBreakPoint(long l) {
        if (!breaks.contains(l)) {
            breaks.add(l);
            for (JavaProgramListener pl : getProgramListeners()) {
                pl.hasBreakPoints(this, breaks);
            }
        }
    }

    public void removeBreakPoint(long l) {
        if (breaks.contains(l)) {
            breaks.remove(l);
            for (JavaProgramListener pl : getProgramListeners()) {
                pl.hasBreakPoints(this, breaks);
            }
        }
    }

    public void hasErrors(TreeMap<Long, String> lines) {
        if (lines.isEmpty()) {
            hasErrors.set(false);
        } else {
            hasErrors.set(true);
        }
        errors.clear();
        errors.putAll(lines);
        for (JavaProgramListener ps : getProgramListeners()) {
            ps.hasErrors(this, errors);
        }
    }

    public ObservableList<Long> getBreakPoints() {
        return breaks;
    }

    public TreeMap<Long, String> getErrors() {
        return errors;
    }

    public void addProgramListener(JavaProgramListener ps) {
        programs.add(ps);
    }

    public List<JavaProgramListener> getProgramListeners() {
        return programs;
    }

    public interface JavaProgramListener {

        public void hasErrors(JavaProgram pro, TreeMap<Long, String> errors);

        public void hasBreakPoints(JavaProgram pro, List<Long> points);
    }

}
