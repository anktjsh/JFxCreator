/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import tachyon.core.DebuggerController;
import tachyon.core.JavaProgram;
import tachyon.core.Program;
import tachyon.core.Project.ErrorReader;
import tachyon.core.Project.OutputReader;
import tachyon.process.ProcessItem;
import tachyon.process.ProcessPool;

/**
 *
 * @author Aniket
 */
public class JavaFileManager extends ParentManager {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static JavaFileManager instance;

    public static JavaFileManager getIsolatedJavaFileManager() {
        if (instance == null) {
            instance = new JavaFileManager();
        }
        return instance;
    }

    public JavaFileManager() {
        super(null);
    }

    @Override
    public void compile(ProcessItem pro) {
    }

    @Override
    public void build(ProcessItem pro) {
    }

    @Override
    public void run(ProcessItem pro) {
    }

    @Override
    public void nativeExecutable(ProcessItem pro) {
    }

    @Override
    public void runIndividualFile(ProcessItem item, Program fa) {
        if (fa instanceof JavaProgram) {
            JavaProgram f = (JavaProgram) fa;
            String name;
            System.out.println(name = getName(f));
            compileIsolatedFile(item, f);
            if (!item.isCancelled()) {
                ProcessBuilder pb = new ProcessBuilder(getRunFileString(name).split(" "));
                pb.directory(new File(".cache"));
                try {
                    Process start = pb.start();
                    item.setName("Launching File " + f.getFile().toAbsolutePath().toString());
                    item.setProcess(start);
                    ProcessPool.getPool().addItem(item);
                    (new Thread(new OutputReader(start.getInputStream(), item.getConsole()))).start();
                    (new Thread(new ErrorReader(start.getErrorStream(), item.getConsole()))).start();
                    int waitFor = start.waitFor();
                    System.out.println(waitFor);
                } catch (IOException | InterruptedException e) {
                }
            }
        }
    }

    private String getName(JavaProgram f) {
        return f.getFileName().substring(0, f.getFileName().lastIndexOf(".java"));
    }

    private static String getRunFileString(String name) {
        String one;
        if (OS.contains("win")) {
            one = "\"" + getJavaHomeLocation()
                    + File.separator + "java\"" + " "
                    + name;
        } else {
            one = getJavaHomeLocation()
                    + File.separator + "java" + " "
                    + name;
        }
        return one;
    }

    private void compileIsolatedFile(ProcessItem item, JavaProgram pro) {
        if (!item.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("mac")) {
                pb = new ProcessBuilder(getMacCompileFileString(pro.getFile()).split(" "));
            } else {
                pb = new ProcessBuilder(getWindowsCompileFileString(pro.getFile()).split(" "));
            }
            pb.directory(pro.getFile().getParent().toFile());
            try {
                Process start = pb.start();
                item.setName("Compiling File " + pro.getFile().toAbsolutePath().toString());
                item.setProcess(start);
                ProcessPool.getPool().addItem(item);
                (new Thread(new OutputReader(start.getInputStream(), item.getConsole()))).start();
                (new Thread(new ErrorReader(start.getErrorStream(), item.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private static String getWindowsCompileFileString(Path a) {
        String one = "\"" + getJavaHomeLocation()
                + File.separator + "javac\"" + " "
                + a.getFileName().toString()
                + " -d " + new File(".cache").getAbsolutePath();
        return one;
    }

    private static String getMacCompileFileString(Path f) {
        String one = getJavaHomeLocation()
                + File.separator + "javac" + " "
                + f.getFileName().toString()
                + " -d " + new File(".cache").getAbsolutePath();
        return one;
    }

    @Override
    public void fatBuild(ProcessItem pro) {
    }

    @Override
    public void debugProject(ProcessItem pro, DebuggerController con) {
    }

    public void launchJar(ProcessItem pro, File jar) {
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsLauncherString(jar);
            } else {
                pb = getMacLauncherString(jar);
            }
            pb.directory(jar.getParentFile());
            try {
                Process start = pb.start();
                pro.setName("Launch File " + jar.getAbsolutePath());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                int waitFor = start.waitFor();
            } catch (IOException | InterruptedException ex) {
            }
        }
    }

    private static ProcessBuilder getWindowsLauncherString(File f) {
        String s = "\"" + getJavaHomeLocation() + File.separator
                + "java\" -jar " + f.getAbsolutePath();
        return new ProcessBuilder(s.split(" "));
    }

    private static ProcessBuilder getMacLauncherString(File f) {
        String s = getJavaHomeLocation() + File.separator
                + "java -jar " + f.getAbsolutePath();
        return new ProcessBuilder(s.split(" "));
    }

}
