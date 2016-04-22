/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.analyze;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import tachyon.analyze.Method.Parameter;
import tachyon.core.JavaProgram;
import tachyon.core.Program;
import tachyon.view.Editor;

/**
 *
 * @author Aniket
 */
public class Analyzer {

    public static ArrayList<Option> analyze(String name, String code, int caret, String currentWord) {
        Class clazz;
        if (name.contains(".")) {
            String a = name.substring(0, name.lastIndexOf('.'));
            String b = name.substring(name.lastIndexOf('.') + 1);
            clazz = Class.create(b, a, code);
        } else {
            clazz = Class.create(name, "", code);
        }

        ArrayList<Option> ret = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            ret.add(new Option("Method : " + m.getName(), m.getName()));
        }
        Method m = clazz.identify(caret);
        if (m != null) {
            for (Parameter p : m.getParameters()) {
                ret.add(0, new Option("Variable : " + p.getName(), p.getName()));
            }
        }
        for (String s : clazz.getImports()) {
            ret.add(new Option("Class : " + s, s));
        }
        return ret;
    }

    public static ArrayList<Option> analyze(Editor edit) {
        ArrayList<Option> al = new ArrayList<>();

        File f = new File(".cache" + File.separator + edit.getScript().getProject().getProjectName() + File.separator + "builds");
        if (!f.exists()) {
            f.mkdirs();
        }

        SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject(((JavaProgram) edit.getScript()).getClassName(), edit.getCodeArea().getText());
        JavaFileObject objects[] = new JavaFileObject[]{fileObject};
        ObservableList<JavaFileObject> objs = FXCollections.observableArrayList(objects);
        ArrayList<JavaProgram> prog = new ArrayList<>();
        prog.add((JavaProgram) edit.getScript());
        for (Tab b : edit.getTabPane().getTabs()) {
            if (b instanceof Editor) {
                Editor ed = (Editor) b;
                if (ed.getScript().getProject().equals(edit.getScript().getProject())) {
                    if (ed.getScript() instanceof JavaProgram) {
                        if (!prog.contains((JavaProgram) ed.getScript())) {
                            prog.add((JavaProgram) ed.getScript());
                            objs.add(new DynamicJavaSourceCodeObject(((JavaProgram) ed.getScript()).getClassName(), ed.getCodeArea().getText()));
                        }
                    }
                }
            }
        }
        for (Program p : edit.getScript().getProject().getPrograms()) {
            if (p instanceof JavaProgram) {
                if (!prog.contains((JavaProgram) p)) {
                    prog.add((JavaProgram) p);
                    objs.add(new DynamicJavaSourceCodeObject(((JavaProgram) p).getClassName(), ((JavaProgram) p).getLastCode()));
                }
            }
        }
        for (JavaFileObject obj : objs) {
            DynamicJavaSourceCodeObject dc = (DynamicJavaSourceCodeObject) obj;
//            System.out.println(dc.getName());
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, Locale.US, null);
        DiagnosticCollector<JavaFileObject> diag = new DiagnosticCollector<>();
        CompilationTask task = compiler.getTask(null, standardFileManager, diag, FXCollections.observableArrayList(
                "-d", f.getAbsolutePath()), null, objs);
        boolean status = task.call();
        if (!status) {
            for (Diagnostic c : diag.getDiagnostics()) {
//                System.out.println(c.getLineNumber());
//                System.out.println(c.getCode());
//                System.out.println(c.getSource());
            }
        }
        try {
            standardFileManager.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        String filePath = ".cache" + File.separator + edit.getScript().getProject().getProjectName() + File.separator
                + "builds" + File.separator + replaceAll(((JavaProgram) edit.getScript()).getClassName(), ".", File.separator) + ".class";
        try {
            new URLClassLoader(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {

                @Override
                protected void addURL(URL url) {
                    super.addURL(url); //To change body of generated methods, choose Tools | Templates.
                }

            }.addURL(new File(filePath).toURI().toURL());
            java.lang.Class<?> loadClass = ClassLoader.getSystemClassLoader().loadClass(((JavaProgram) edit.getScript()).getClassName());
            for (java.lang.reflect.Method m : loadClass.getMethods()) {
                al.add(new Option(m.getName(), m.getName()));
            }
//        try {
//            java.lang.Class<?> classs = ClassLoader.getSystemClassLoader().loadClass(edit.getScript().getClassName());
//            System.out.println(classs.getName());
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
//        }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return al;
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

    public static class Option {

        private final String caption;
        private final String realText;

        public Option(String a, String b) {
            caption = a;
            realText = b;
        }

        /**
         * @return the caption
         */
        public String getCaption() {
            return caption;
        }

        /**
         * @return the realText
         */
        public String getRealText() {
            return realText;
        }

        @Override
        public String toString() {
            return getCaption();
        }
    }

    private static class DynamicJavaSourceCodeObject extends SimpleJavaFileObject {

        private String name;
        private String source;

        public DynamicJavaSourceCodeObject(String name, String code) {
            super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.name = name;
            this.source = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return getCode();
        }

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
