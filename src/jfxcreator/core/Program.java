/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class Program {

    public static final int JAVA = 0, RESOURCE = 1;

    private final Path file;
    private List<String> lastCode;
    private final Project project;
    private final int type;
    private String className;
    private final ObservableList<ProgramListener> programs;
    private final BooleanProperty hasErrors;

    private final HashMap<String, List<String>> previous = new HashMap<>();

    public Program(int t, String name, Path p, List<String> cod, Project pro) {
        type = t;
        file = p;
        lastCode = cod;
        project = pro;
        className = name;
        if (t == JAVA) {
            if (!className.contains(".")) {
                verifyClassName();
            }
        }
        programs = FXCollections.observableArrayList();
        hasErrors = new SimpleBooleanProperty(false);
        initProgram(cod);
    }

    public Program(int t, Path p, List<String> cod, Project pro) {
        this(t, p.getFileName().toString().substring(0, p.getFileName().toString().lastIndexOf(".")), p, cod, pro);
    }

    public String getClassName() {
        return className;
    }

    private void verifyClassName() {
        if (getProject() != null) {
            String src = getProject().getSource().toAbsolutePath().toString();
            String name = file.toAbsolutePath().toString().substring(0, file.toAbsolutePath().toString().lastIndexOf(".java"));
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

    public static String getFilePath(String className) {
        while (className.contains(".")) {
            String one = className.substring(0, className.indexOf('.'));
            String two = className.substring(className.indexOf('.') + 1);
            className = one + File.separator + two;
        }
        return File.separator + className;
    }

    private void initProgram(List<String> code) {
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(getFile().getParent());
                Files.createFile(getFile());
            } catch (IOException ex) {
            }
            try {
                Files.write(getFile(), code);
            } catch (IOException ex) {
            }
        } else {
            lastCode = readCode();
        }
    }

    public void reload() {
        lastCode = readCode();
    }

    public boolean canSave(List<String> code) {
        return !code.equals(lastCode);
    }

    public String getCurrentTime() {
        return LocalDate.now().toString() + " " + LocalTime.now().toString().replaceAll(":", "-");
    }

    public List<String> previousSavedDates() {
        ArrayList<String> al = new ArrayList<>();
        if (getProject() == null) {
            return al;
        }
        File se = new File(".cache" + File.separator
                + getProject().getProjectName() + File.separator + "previous"
                + File.separator + getClassName() + File.separator);
        if (se.exists()) {
            for (File f : se.listFiles()) {
                al.add(f.getName().substring(0, f.getName().indexOf(".txt")));
            }
        }
        return al;
    }

    public List<String> getPreviousCode(String date) {
        ArrayList<String> al = new ArrayList<>();
        if (getProject() == null) {
            return al;
        }
        File se = new File(".cache" + File.separator
                + getProject().getProjectName() + File.separator + "previous"
                + File.separator + getClassName() + File.separator);
        if (se.exists()) {
            if (previous.containsKey(date)) {
                return previous.get(date);
            } else {
                for (File f : se.listFiles()) {
                    if (f.getName().contains(date)) {
                        try {
                            List<String> read = Files.readAllLines(f.toPath());
                            previous.put(date, read);
                            al.addAll(read);
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
        return al;
    }

    public void save(List<String> code) {
        if (getProject() != null) {
            try {
                Path get = Paths.get(".cache" + File.separator
                        + getProject().getProjectName() + File.separator + "previous" + File.separator + getClassName()
                        + File.separator + getCurrentTime() + ".txt");
                if (!Files.exists(get)) {
                    Files.createDirectories(get.getParent());
                }
                Files.write(get, getLastCode());
            } catch (IOException e) {
            }
        }
        try {
            Files.write(file, code);
        } catch (IOException ex) {
        }
        lastCode = code;
    }

    private List<String> readCode() {
        try {
            return Files.readAllLines(getFile());
        } catch (IOException ex) {
        }
        return new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Program) {
            Program pro = (Program) obj;
            if (pro.getFile().equals(getFile())) {
                return true;
            }
        }
        return false;
    }

    public Path getFile() {
        return file;
    }

    public List<String> getLastCode() {
        return lastCode;
    }

    public String getCode() {
        StringBuilder sb = new StringBuilder();
        for (String s : getLastCode()) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    public Project getProject() {
        return project;
    }

    public int getType() {
        return type;
    }

    public void hasErrors(TreeMap<Long, String> lines) {
        if (lines.isEmpty()) {
            hasErrors.set(false);
        } else {
            hasErrors.set(true);
        }
        for (ProgramListener ps : getProgramListeners()) {
            ps.hasErrors(this, lines);
        }
    }

    public void addProgramListener(ProgramListener ps) {
        programs.add(ps);
    }

    public List<ProgramListener> getProgramListeners() {
        return programs;
    }

    public interface ProgramListener {

        public void hasErrors(Program pro, TreeMap<Long, String> errors);
    }
}
