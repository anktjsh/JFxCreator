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
import java.util.ArrayList;
import java.util.List;

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
        initProgram(cod);
    }

    public Program(int t, Path p, List<String> cod, Project pro) {
        this(t, p.getFileName().toString().substring(0, p.getFileName().toString().lastIndexOf(".java")), p, cod, pro);
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

    public void save(List<String> code) {
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

    public Project getProject() {
        return project;
    }

    public int getType() {
        return type;
    }
}
