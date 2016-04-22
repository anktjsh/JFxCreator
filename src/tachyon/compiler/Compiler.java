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
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import tachyon.core.JavaLibrary;
import tachyon.core.JavaProgram;
import tachyon.core.Program;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public abstract class Compiler {

    private final Project total;
    private final ObservableList<String> compilerOptions;
    private StandardJavaFileManager standard;
    private final DiagnosticCollector<JavaFileObject> diag;

    private static JavaCompiler compiler;

    static {
        try {
            compiler = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public Compiler(Project pro) {
        total = pro;

        standard = compiler.getStandardFileManager(null, Locale.getDefault(), null);
        diag = new DiagnosticCollector<>();
        compilerOptions = FXCollections.observableArrayList();
    }

    public void recreateFileManager() {
        try {
            standard.close();
        } catch (IOException e) {
        }
        standard = compiler.getStandardFileManager(null, Locale.getDefault(), null);
    }

    public StandardJavaFileManager getFileManager() {
        return standard;
    }

    public List<String> getCompilerOptions() {
        return compilerOptions;
    }

    public DiagnosticCollector<JavaFileObject> getDiagnosticCollector() {
        return diag;
    }

    public Project getProject() {
        return total;
    }

    public JavaCompiler getCompiler() {
        return compiler;
    }

    public abstract void prepare();

    public JavaProgram getProgram(String key) {
        String uri = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
        uri = uri.replace("string:///", "");
        uri = replaceAll(uri, "/", ".");
        uri = uri.substring(0, uri.indexOf(".java"));
        for (Program pp : total.getPrograms()) {
            if (pp instanceof JavaProgram) {
                JavaProgram p = (JavaProgram) pp;
                if (p.getClassName().equals(uri)) {
                    return p;
                }
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

    static class DynamicJavaSourceCodeObject extends SimpleJavaFileObject {

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
