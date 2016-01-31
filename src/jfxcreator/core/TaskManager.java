/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jfxcreator.view.Dependencies;

/**
 *
 * @author Aniket
 */
public class TaskManager {

    private final Project project;

    public TaskManager(Project pro) {
        project = pro;
    }

    public void compile(ProcessItem pro) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = getWindowsCompileString();
            } else {
                pb = getMacCompileString();
            }
            pb.directory(project.getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Compile Files for Project " + project.getProjectName());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private ProcessBuilder getWindowsCompileString() {
        String one = "\"" + getJavaHomeLocation() + File.separator + "javac\""
                + project.getFileList() + " -d "
                + project.getBuild().toAbsolutePath().toString()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    private ProcessBuilder getMacCompileString() {
        String one = getJavaHomeLocation() + File.separator + "javac"
                + project.getFileList()
                + " -d "
                + project.getBuild().toAbsolutePath().toString()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    public void build(ProcessItem pro) {
        compile(pro);
        String os = System.getProperty("os.name").toLowerCase();
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = getWindowsBuildString();
            } else {
                pb = getMacBuildString();
            }
            pb.directory(project.getRootDirectory().toFile());

            try {
                Process start = pb.start();
                pro.setName("Build Jar File for Project " + project.getProjectName());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private ProcessBuilder getWindowsBuildString() {
        String one = "\"" + getJavaHomeLocation() + File.separator + "javapackager\"" + " -createjar -appclass " + project.getMainClassName()
                + " -srcdir " + project.getBuild().toAbsolutePath().toString() + " -outdir "
                + project.getDist().toAbsolutePath().toString() + " -outfile " + project.getProjectName() + ".jar"
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    private ProcessBuilder getMacBuildString() {
        String one = getJavaHomeLocation() + File.separator + "javapackager" + " -createjar -appclass " + project.getMainClassName()
                + " -srcdir " + project.getBuild().toAbsolutePath().toString() + " -outdir "
                + project.getDist().toAbsolutePath().toString() + " -outfile " + project.getProjectName() + ".jar"
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    public void run(ProcessItem pro) {
        build(pro);
        String os = System.getProperty("os.name").toLowerCase();
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
        }
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = getWindowsRunString();
            } else {
                pb = getMacRunString();
            }
            pb.directory(project.getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Launching Jar File for Project " + project.getProjectName());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private ProcessBuilder getWindowsRunString() {
        String one = "\"" + getJavaHomeLocation()
                + File.separator + "java\"" + " -jar " + project.getDist().getFileName().toString()
                + File.separator + project.getProjectName() + ".jar"
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    private ProcessBuilder getMacRunString() {
        String one = getJavaHomeLocation()
                + File.separator + "java" + " -jar " + project.getDist().getFileName().toString()
                + File.separator + project.getProjectName() + ".jar"
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    public void runFile(ProcessItem item, Program program) {
        String OS = System.getProperty("os.name").toLowerCase();
        compile(item);
        if (!item.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsRunFileString(program);
            } else {
                pb = getMacRunFileString(program);
            }
            pb.directory(project.getBuild().toFile());
            try {
                Process start = pb.start();
                item.setName("Launching File : " + program.getClassName());
                item.setProcess(start);
                ProcessPool.getPool().addItem(item);
                (new Thread(new Project.OutputReader(start.getInputStream(), item.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), item.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private ProcessBuilder getWindowsRunFileString(Program program) {
        return new ProcessBuilder("\"" + getJavaHomeLocation() + File.separator + "java\"", program.getClassName()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList())));
    }

    private ProcessBuilder getMacRunFileString(Program program) {
        return new ProcessBuilder(getJavaHomeLocation() + File.separator + "java", program.getClassName()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList())));
    }

    public void fatJar(ProcessItem item) throws IOException {
        build(item);
        Path fat = Paths.get(project.getRootDirectory().toAbsolutePath().toString() + File.separator + "bundle" + File.separator + project.getProjectName() + ".jar");
        if (!Files.exists(fat.getParent())) {
            try {
                Files.createDirectories(fat.getParent());
            } catch (IOException ex) {
            }
        }
        almostDeepDelete(new File(fat.getParent().toAbsolutePath().toString()));
        String input = project.getDist().toAbsolutePath().toString() + File.separator + project.getProjectName() + ".jar";
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = fat.getParent().toAbsolutePath().toString() + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractToFile(zipIn, filePath);
                } else {
                    Path dir = Paths.get(filePath);
                    Files.createDirectory(dir);
                }
                entry = zipIn.getNextEntry();
            }
        }

        for (JavaLibrary lib : project.getAllLibs()) {
            ZipInputStream zipIn = lib.getBinaryZipInputStream();
            if (zipIn != null) {
                ZipEntry entry = zipIn.getNextEntry();
                while (entry != null) {
                    String filePath = fat.getParent().toAbsolutePath().toString() + File.separator + entry.getName();
                    if (!entry.isDirectory()) {
                        extractToFile(zipIn, filePath);
                    } else {
                        Path dir = Paths.get(filePath);
                        Files.createDirectory(dir);
                    }
                    entry = zipIn.getNextEntry();
                }
            }
        }
        Path mani = Paths.get(project.getRootDirectory().toAbsolutePath().toString() + File.separator + "bundle" + File.separator + "META-INF" + File.separator
                + "MANIFEST.MF");
        if (Files.exists(mani)) {
            Files.delete(mani);
        }
        buildFat(item);
        deepDelete(fat.getParent());
    }

    private void extractToFile(ZipInputStream sipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = sipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void almostDeepDelete(File p) {
        if (p.isDirectory()) {
            for (File f : p.listFiles()) {
                deepDelete(f.toPath());
            }
        }
    }

    private void deepDelete(Path fe) {
        if (!Files.exists(fe)) {
            return;
        }
        if (Files.isDirectory(fe)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(fe)) {
                for (Path run : stream) {
                    deepDelete(run);
                }
            } catch (IOException | DirectoryIteratorException x) {
            }
            try {
                Files.delete(fe);
            } catch (IOException ex) {
            }
        } else {
            try {
                Files.delete(fe);
            } catch (IOException ex) {
            }
        }
    }

    private void buildFat(ProcessItem pro) {
        String OS = System.getProperty("os.name").toLowerCase();
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsFatJarString();
            } else {
                pb = getMacFatJarString();
            }
            pb.directory(project.getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Combining All Existing Jars for Project " + project.getProjectName());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private ProcessBuilder getWindowsFatJarString() {
        return new ProcessBuilder("\"" + getJavaHomeLocation() + File.separator + "javapackager\"",
                "-createjar",
                "-appClass", project.getMainClassName(),
                "-srcdir", "bundle",
                "-outdir", "dist",
                "-outfile", "bundle.jar", "-v");
    }

    private ProcessBuilder getMacFatJarString() {
        return new ProcessBuilder(getJavaHomeLocation() + File.separator + "javapackager",
                "-createjar",
                "-appClass", project.getMainClassName(),
                "-srcdir", "bundle",
                "-outdir", "dist",
                "-outfile", "bundle.jar", "-v");
    }

    public void nativeExecutable(ProcessItem pro) {
        build(pro);
        String os = System.getProperty("os.name").toLowerCase();
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = getWindowsExectuableString();
            } else {
                pb = getMacExectuableString();
            }
            pb.directory(project.getDist().toFile());
            try {
                Process start = pb.start();
                pro.setName("Compile Native for Project " + project.getRootDirectory().getFileName().toString());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                int waitFor = start.waitFor();
            } catch (IOException | InterruptedException ex) {
            }
        }
    }

    private ProcessBuilder getWindowsExectuableString() {
        String ico = null;
        String a = "\"" + getJavaHomeLocation() + File.separator + "javapackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + project.getDist().toAbsolutePath().toString()
                + " -outfile " + project.getProjectName() + " -srcdir " + project.getDist().toAbsolutePath().toString() + " -srcFiles " + project.getProjectName()
                + ".jar " + " -appclass " + project.getMainClassName() + " -name " + project.getProjectName() + " -title " + project.getProjectName() + " -v";
        return new ProcessBuilder(a.split(" "));
    }

    private ProcessBuilder getMacExectuableString() {
        String ico = null;
        String a = getJavaHomeLocation() + File.separator + "javapackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + project.getDist().toAbsolutePath().toString()
                + " -outfile " + project.getProjectName() + " -srcdir " + project.getDist().toAbsolutePath().toString() + " -srcFiles " + project.getProjectName()
                + ".jar " + "-appclass " + project.getMainClassName() + " -name " + project.getProjectName() + " -title " + project.getProjectName() + " mac.CFBundleName=" + project.getProjectName() + " -v";
        return new ProcessBuilder(Arrays.asList(a.split(" ")));
    }

    public void debugProject(ProcessItem pro, DebuggerController controller) {
        compile(pro);
        String os = System.getProperty("os.name").toLowerCase();
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = getWindowsDebuggerString();
            } else {
                pb = getMacDebuggerString();
            }
            pb.directory(project.getBuild().toFile());
            try {
                Process start = pb.start();
                pro.setName("Debug Project " + project.getRootDirectory().getFileName().toString());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
                controller.setOutputStream(start.getOutputStream());
                int waitFor = start.waitFor();
                controller.finished();
            } catch (IOException | InterruptedException ex) {
            }
        }
    }

    private ProcessBuilder getWindowsDebuggerString() {
        String s = "\"" + getJavaHomeLocation() + File.separator + "jdb\" " + project.getMainClassName()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(s.split(" "));
    }

    private ProcessBuilder getMacDebuggerString() {
        String s = getJavaHomeLocation() + File.separator + "jdb " + project.getMainClassName()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(s.split(" "));
    }

    private String getJavaHomeLocation() {
        return Dependencies.localVersionProperty.get();
    }

}
