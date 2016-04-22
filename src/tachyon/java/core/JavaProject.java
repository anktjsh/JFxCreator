/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.java.core;

import tachyon.java.core.JavaProgram;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tachyon.framework.core.Program;
import tachyon.framework.core.Project;
import tachyon.framework.manager.TaskManager;
import tachyon.view.LibraryTreeItem.LibraryListener;

/**
 *
 * @author Aniket
 */
public abstract class JavaProject extends Project {

    private final Path libs, build;
    private LibraryListener ll;
    private final ObservableList<JavaLibrary> allLibs;
    private final HashMap<String, String> compileArguments;
    private final ArrayList<String> runtimeArguments;
    private String mainClassName;
    private String iconFilePath;

    public JavaProject(Path rot, boolean isNew, String mcn) {
        super(rot, isNew);
        mainClassName = mcn;
        libs = Paths.get(getRootDirectory().toAbsolutePath().toString() + File.separator + "libs");
        build = Paths.get(getRootDirectory().toAbsolutePath().toString() + File.separator + "build");
        if (!Files.exists(libs)) {
            try {
                Files.createDirectories(libs);
            } catch (IOException ex) {
            }
        }
        if (!Files.exists(build)) {
            try {
                Files.createDirectories(build);
            } catch (IOException e) {
            }
        }
        allLibs = FXCollections.observableArrayList();
        compileArguments = new HashMap<>();
        runtimeArguments = new ArrayList<>();
        if (isNew) {
            initializeProject();
        } else {
            addExistingPrograms();
        }
        if (!Files.exists(getConfig())) {
            saveConfig();
        } else {
            readConfig();
        }
    }

    public Path getBuild() {
        return build;
    }

    public Path getLibs() {
        return libs;
    }

    public LibraryListener getLibraryListener() {
        return ll;
    }

    public void setLibraryListener(LibraryListener ll) {
        this.ll = ll;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String main) {
        mainClassName = main;
    }

    @Override
    public String serialize() {
        return "Project : " + getRootDirectory().toAbsolutePath().toString() + " : " + getMainClassName();
    }

    @Override
    protected abstract TaskManager constructManager();

    private void addAllPaths(ArrayList<Path> al, File p) {
        if (p.isDirectory()) {
            for (File f : p.listFiles()) {
                addAllPaths(al, f);
            }
        } else {
            al.add(p.toPath());
        }
    }

    @Override
    protected void checkAll() {
        ArrayList<Program> remove = new ArrayList<>();
        ArrayList<Path> add = new ArrayList<>();
        addAllPaths(add, getSource().toFile());
        for (Path p : add) {
            System.out.println("All : " + p.toAbsolutePath().toString());
        }
        for (Program p : getPrograms()) {
            if (!add.contains(p.getFile())) {
                remove.add(p);
            } else {
                add.remove(p.getFile());
            }
        }
        for (Program p : getPrograms()) {
            System.out.println("program : " + p.getFile().toAbsolutePath().toString());
        }
        for (Program p : remove) {
            System.out.println("Remove : " + p.getFile().toAbsolutePath().toString());
            removeScript(p);
            System.out.println("Removed");
        }
        for (Path p : add) {
            System.out.println("Add : " + p.toAbsolutePath().toString());
            addScriptsToList(p.toFile(), true);
            System.out.println("Added");
        }
    }

    @Override
    protected abstract void initializeProject();

    @Override
    protected void readConfig() {
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(getConfig()));
        } catch (IOException ex) {
        }
        if (al.size() >= 2) {
            if (!al.get(1).isEmpty()) {
                String spl[] = al.get(1).split(" : ");
                String liberator = spl[1].substring(1, spl[1].length() - 1);
                List<String> list = Arrays.asList(liberator.split(", "));
                for (String s : list) {
                    if (!s.isEmpty()) {
                        allLibs.add(new JavaLibrary(s));
                    }
                }
            }
        }
        if (al.size() >= 3) {
            if (!al.get(2).isEmpty()) {
                String check = al.get(2).substring(1, al.get(2).length() - 1);
                List<String> list = Arrays.asList(check.split(", "));
                for (String s : list) {
                    String spl[] = s.split("=");
                    if (spl.length == 2) {
                        compileArguments.put(spl[0], spl[1]);
                    }
                }
            }
        }
        if (al.size() >= 4) {
            if (!al.get(3).isEmpty()) {
                String check = al.get(3).substring(1, al.get(3).length() - 1);
                List<String> list = Arrays.asList(check.split(", "));
                if (!list.isEmpty()) {
                    if (!list.get(0).isEmpty()) {
                        runtimeArguments.addAll(list);
                    }
                }
            }
        }
        if (al.size() >= 5) {
            if (!al.get(4).isEmpty()) {
                iconFilePath = al.get(4);
            }
        }
    }

    @Override
    protected abstract void saveConfig();

    @Override
    protected void addExistingPrograms() {
        for (File f : getSource().toFile().listFiles()) {
            addScriptsToList(f, false);
        }
    }

    public HashMap<String, String> getCompileTimeArguments() {
        return compileArguments;
    }

    public ArrayList<String> getRuntimeArguments() {
        return runtimeArguments;
    }

    protected String getIconFilePath() {
        return iconFilePath;
    }

    private String getClassName(File f) {
        String name = f.getAbsolutePath().replace(getSource().toFile().getAbsolutePath(), "");
        name = name.substring(0, name.lastIndexOf(".java"));
        return JavaProgram.getClassName(name);
    }

    private void addScriptsToList(File f, boolean b) {
        if (!f.isDirectory()) {
            if (f.getName().endsWith(".java")) {
                if (b) {
                    addScript(new JavaProgram(f.toPath(), new ArrayList<>(), this, getClassName(f)));
                } else {
                    addProgram(new JavaProgram(f.toPath(), new ArrayList<>(), this, getClassName(f)));
                }
            } else if (b) {
                addScript(new Resource(f.toPath(), new ArrayList<>(), this));
            } else {
                addProgram(new Resource(f.toPath(), new ArrayList<>(), this));
            }
        } else {
            for (File fa : f.listFiles()) {
                addScriptsToList(fa, b);
            }
        }
    }

    public String getFileIconPath() {
        if (iconFilePath == null) {
            iconFilePath = "";
        }
        return iconFilePath;
    }

    public void setFileIconPath(String s) {
        iconFilePath = s;
    }

    public void setCompileTimeArguments(HashMap<String, String> map) {
        compileArguments.clear();
        compileArguments.putAll(map);
    }

    public void setRuntimeArguments(List<String> map) {
        runtimeArguments.clear();
        runtimeArguments.addAll(map);
    }

    public List<JavaLibrary> getAllLibs() {
        return allLibs;
    }

    public void setAllLibs(List<String> all) {
        for (File f : libs.toFile().listFiles()) {
            f.delete();
        }
        all.stream().map((s) -> Paths.get(s)).forEach((p) -> {
            try {
                Files.copy(p, Paths.get(libs.toAbsolutePath().toString() + File.separator + p.getFileName().toString()));
            } catch (IOException ex) {
            }
        });
        allLibs.clear();
        for (String s : all) {
            allLibs.add(new JavaLibrary(s));
        }
        if (ll != null) {
            ll.librariesChanged(all);
        }
        saveConfig();
    }

    public String getCompileList() {
        StringBuilder sb = new StringBuilder();
        for (String s : compileArguments.keySet()) {
            sb.append(" ").append(s).append(":").append(compileArguments.get(s));
        }
        return sb.toString();
    }

    public String getRuntimeList() {
        StringBuilder sb = new StringBuilder();
        for (String s : runtimeArguments) {
            sb.append(" ").append(s);
        }
        return sb.toString();
    }

    public String getFileList() {
        StringBuilder sb = new StringBuilder();
        for (Program p : getPrograms()) {
            if (p instanceof JavaProgram) {
                sb.append(" ").append(p.getFile().toAbsolutePath().toString());
            }
        }
        return sb.toString();
    }

    public int getNumLibs() {
        return allLibs.size();
    }

    public String getLibsList() {
        StringBuilder sb = new StringBuilder();
        File f = new File(getRootDirectory().toAbsolutePath().toString() + File.separator + "libs");
        for (File file : f.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                sb.append(" ").append(file.getAbsolutePath());
            }
        }
        return sb.toString();
    }

}
