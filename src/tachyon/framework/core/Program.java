/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.framework.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Aniket
 */
public abstract class Program {

    private final Path file;
    private String lastCode;
    private final Project project;
    private final HashMap<String, List<String>> previous = new HashMap<>();

    public Program(Path p, List<String> cod, Project pro) {
        file = p;
        lastCode = convertToString(cod);
        project = pro;
        init(cod);
    }

    private void init(List<String> code) {
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
            reload();
        }
    }

    public void reload() {
        lastCode = readCode();
    }

    private String readCode() {
        StringBuilder sb = new StringBuilder();
        try {
            Scanner in = new Scanner(getFile());
            while (in.hasNextLine()) {
                sb.append(in.nextLine()).append("\n");
            }
        } catch (IOException e) {
        }
        return sb.toString();
    }

    public Path getFile() {
        return file;
    }

    private String convertToString(List<String> cod) {
        StringBuilder sb = new StringBuilder();
        for (String s : cod) {
            sb.append(s).append("\n");
        }
        return sb.toString();
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
                + File.separator + getFileName() + File.separator);
        if (se.exists()) {
            for (File f : se.listFiles()) {
                al.add(f.getName().substring(0, f.getName().indexOf(".txt")));
            }
        }
        return al;
    }

    public void deletePrevious(String date) {
        previous.remove(date);
        File se = new File(".cache" + File.separator
                + getProject().getProjectName() + File.separator + "previous"
                + File.separator + getFileName() + File.separator);
        File det = null;
        if (se.exists()) {
            for (File f : se.listFiles()) {
                if (f.getName().contains(date)) {
                    det = f;
                    break;
                }
            }
            if (det != null) {
                det.delete();
            }
        }
    }

    public List<String> getPreviousCode(String date) {
        ArrayList<String> al = new ArrayList<>();
        if (getProject() == null) {
            return al;
        }
        File se = new File(".cache" + File.separator
                + getProject().getProjectName() + File.separator + "previous"
                + File.separator + getFileName() + File.separator);
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

    public boolean canSave(String text) {
        return !lastCode.contains(text);
    }

    public void save(String code) {
        if (getProject() != null) {
            try {
                Path get = Paths.get(".cache" + File.separator
                        + getProject().getProjectName() + File.separator + "previous" + File.separator + getFileName()
                        + File.separator + getCurrentTime() + ".txt");
                if (!Files.exists(get)) {
                    Files.createDirectories(get.getParent());
                }
                Files.write(get, Arrays.asList(getLastCode().split("\n")));
            } catch (IOException e) {
            }
        }
        try {
            Files.write(file, Arrays.asList(code.split("\n")));
        } catch (IOException ex) {
        }
        lastCode = code;
    }

    public String getLastCode() {
        return lastCode;
    }

    public Project getProject() {
        return project;
    }

    public String getFileName() {
        return getFile().getFileName().toString();
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

    public static String getFilePath(String className) {
        while (className.contains(".")) {
            String one = className.substring(0, className.indexOf('.'));
            String two = className.substring(className.indexOf('.') + 1);
            className = one + File.separator + two;
        }
        return File.separator + className;
    }

}
