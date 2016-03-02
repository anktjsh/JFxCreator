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
public class Compiler {

    private static JavaCompiler compiler;

    static {
        try {
            compiler = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    private final Project total;
    private final ObservableList<Editor> allEditors;
    private final ObservableList<Program> allPrograms;

    private final ObservableList<String> compilerOptions;
    private StandardJavaFileManager standard;
    private final DiagnosticCollector<JavaFileObject> diag;

    public Compiler(Editor edit) {
        total = edit.getProject();
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
        for (Program p : total.getPrograms()) {
            if (!al.contains(p)) {
                if (p.getType() == Program.JAVA) {
                    allPrograms.add(p);
                }
            }
        }

        standard = compiler.getStandardFileManager(null, Locale.getDefault(), null);
        diag = new DiagnosticCollector<>();
        compilerOptions = FXCollections.observableArrayList();
    }

    public void prepare() {
        ObservableList<DynamicJavaSourceCodeObject> objs = FXCollections.observableArrayList();
        allEditors.stream().forEach((ed) -> {
            objs.add(new DynamicJavaSourceCodeObject(ed.getScript().getClassName(), ed.getCodeArea().getText()));
        });
        for (Program p : allPrograms) {
            objs.add(new DynamicJavaSourceCodeObject(p.getClassName(), p.getLastCode()));
        }
        System.out.println(allPrograms.size());
        System.out.println(objs.size());
        JavaCompiler.CompilationTask task = compiler.getTask(null, standard, diag, compilerOptions, null, objs);
        HashMap<String, TreeMap<Long, String>> map = new HashMap<>();
        boolean status = task.call();
        if (!status) {
            for (Diagnostic c : diag.getDiagnostics()) {
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
        for (Program p : total.getPrograms()) {
            if (!sent.contains(p)) {
                p.hasErrors(new TreeMap<>());
            }
        }
        try {
            standard.close();
        } catch (IOException e) {
        }
        standard = compiler.getStandardFileManager(null, Locale.getDefault(), null);
        compilerOptions.clear();
    }

    public Program getProgram(String key) {
        String uri = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
        uri = uri.replace("string:///", "");
        uri = replaceAll(uri, "/", ".");
        uri = uri.substring(0, uri.indexOf(".java"));
        for (Program p : total.getPrograms()) {
            if (p.getClassName().equals(uri)) {
                return p;
            }
        }
        return null;
    }

    public void setDirectory(File filepath) {
        filepath.mkdirs();
        addCompilerOptions("-d", filepath.getAbsolutePath());
    }

    public void addToClassPath(List<JavaLibrary> a) {
        try {
            ArrayList<File> al = new ArrayList<>();
            for (JavaLibrary jl : a) {
                al.add(jl.getBinaryPath().toFile());
            }
            standard.setLocation(StandardLocation.CLASS_PATH, al);
        } catch (IOException ex) {
        }
    }

    public void addCompilerOptions(String... options) {
        addCompilerOptions(Arrays.asList(options));
    }

    public void addCompilerOptions(List<String> options) {
        compilerOptions.addAll(options);
    }

    private static String replaceAll(String txt, String init, String repl) {
        while (txt.contains(init)) {
            int index = txt.indexOf(init);
            String first = txt.substring(0, index);
            String two = txt.substring(index + 1);
            txt = first + repl + two;
        }
        return txt;
    }

    private static class DynamicJavaSourceCodeObject extends SimpleJavaFileObject {

        private String name;
        private String source;

        public DynamicJavaSourceCodeObject(String name, String code) {
            super(URI.create("string:///" + name.replaceAll("\\.", "/") + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.name = name;
            this.source = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return getCode();
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String s) {
            name = s;
        }

        public String getCode() {
            return source;
        }

        public void setCode(String s) {
            source = s;
        }
    }
}