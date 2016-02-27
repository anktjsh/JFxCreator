/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.core;

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
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import tachyon.view.Dependencies;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;

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
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()))
                + project.getCompileList();
        return new ProcessBuilder(one.split(" "));
    }

    private ProcessBuilder getMacCompileString() {
        String one = getJavaHomeLocation() + File.separator + "javac"
                + project.getFileList()
                + " -d "
                + project.getBuild().toAbsolutePath().toString()
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()))
                + project.getCompileList();
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
                + File.separator + project.getProjectName() + ".jar" + (project.getRuntimeList())
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    private ProcessBuilder getMacRunString() {
        String one = getJavaHomeLocation()
                + File.separator + "java" + " -jar " + project.getDist().getFileName().toString()
                + File.separator + project.getProjectName() + ".jar" + (project.getRuntimeList())
                + (project.getAllLibs().isEmpty() ? "" : (" -classpath" + project.getLibsList()));
        return new ProcessBuilder(one.split(" "));
    }

    public static void runIsolatedFile(ProcessItem item, Program f) {
        String name;
        System.out.println(name = getName(f));
        compileFile(item, f);
        if (!item.isCancelled()) {
            String OS = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = new ProcessBuilder(getWindowsRunFileString(name).split(" "));
            } else {
                pb = new ProcessBuilder(getMacRunFileString(name).split(" "));
            }
            pb.directory(new File(".cache"));
            try {
                Process start = pb.start();
                item.setName("Launching File " + f.getFile().toAbsolutePath().toString());
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

    private static String getName(Program f) {
        String total = f.getLastCode();
        int packIndex = total.indexOf("package");
        int previousCount = 0;
        while (frequency(total.substring(0, packIndex), "/*") != frequency(total.substring(0, packIndex), "*/")) {
            previousCount += packIndex + "package".length();
            String temp = total.substring(previousCount);
            packIndex = previousCount + temp.indexOf("package");
        }
        String one = total.substring(0, packIndex);
        String two = total.substring(packIndex);
        two = two.substring(0, two.indexOf(";")+1);
        String packName = total.substring(packIndex + "package".length() + 1, one.length() + two.indexOf(";")).trim().replaceAll(" ", "")+"." +
                f.getFile().getFileName().toString().replace(".java", "");
        return packName;
    }

    private static int frequency(String total, String search) {
        int count = 0;
        String alias = total;
        while (alias.contains(search)) {
            int index = alias.indexOf(search);
            alias = alias.substring(index + search.length());
            count++;
        }
        return count;
    }

    private static String getWindowsRunFileString(String name) {
        String one = "\"" + getJavaHomeLocation()
                + File.separator + "java\"" + " "
                + name;
        return one;
    }

    private static String getMacRunFileString(String name) {
        String one = getJavaHomeLocation()
                + File.separator + "java" + " "
                + name;
        return one;
    }
//

    private static void compileFile(ProcessItem item, Program pro) {
        if (!item.isCancelled()) {
            String OS = System.getProperty("os.name").toLowerCase();
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
                (new Thread(new Project.OutputReader(start.getInputStream(), item.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), item.getConsole()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }
//    

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
                pb = getWindowsExecutableString();
            } else {
                pb = getMacExecutableString();
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

    private ProcessBuilder getWindowsExecutableString() {
        String ico = getIconPath();
        String a = "\"" + getJavaHomeLocation() + File.separator + "javapackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + project.getDist().toAbsolutePath().toString()
                + " -outfile " + project.getProjectName() + " -srcdir " + project.getDist().toAbsolutePath().toString() + " -srcFiles " + project.getProjectName()
                + ".jar " + " -appclass " + project.getMainClassName() + " -name " + project.getProjectName() + " -title " + project.getProjectName() + " -v";
        return new ProcessBuilder(a.split(" "));
    }

    private ProcessBuilder getMacExecutableString() {
        String ico = getIconPath();
        String a = getJavaHomeLocation() + File.separator + "javapackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + project.getDist().toAbsolutePath().toString()
                + " -outfile " + project.getProjectName() + " -srcdir " + project.getDist().toAbsolutePath().toString() + " -srcFiles " + project.getProjectName()
                + ".jar " + "-appclass " + project.getMainClassName() + " -name " + project.getProjectName() + " -title " + project.getProjectName() + " mac.CFBundleName=" + project.getProjectName() + " -v";
        return new ProcessBuilder(Arrays.asList(a.split(" ")));
    }

    private String getIconPath() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (project.getFileIconPath().isEmpty()) {
            return null;
        }
        if (OS.contains("win")) {
            if (project.getFileIconPath().endsWith(".ico")) {
                return project.getFileIconPath();
            } else {
                File f = new File(project.getFileIconPath());
                if (f.exists()) {
                    File to = new File(project.getDist().toAbsolutePath().toString() + File.separator + getFilename(f) + ".ico");
                    if (!to.exists()) {
                        try {
                            Image im = new Image(f.toURI().toString(), 256, 256, true, true);
                            ICOEncoder.write(SwingFXUtils.fromFXImage(im, null), to);
                            if (to.exists()) {
                                return to.getAbsolutePath();
                            }
                        } catch (IOException ex) {
                        }
                    } else {
                        return to.getAbsolutePath();
                    }
                }
            }
        } else {
            if (project.getFileIconPath().endsWith(".icns")) {
                return project.getFileIconPath();
            } else {
                File f = new File(project.getFileIconPath());
                if (f.exists()) {
                    File to = new File(project.getDist().toAbsolutePath().toString() + File.separator + getFilename(f) + ".icns");
                    if (!to.exists()) {
                        try {
                            Image im = new Image(f.toURI().toString());
                            Imaging.writeImage(SwingFXUtils.fromFXImage(im, null), f, null, null);
                            return to.getAbsolutePath();
                        } catch (IOException | ImageWriteException ex) {
                        }
                    } else {
                        return to.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private String getFilename(File f) {
        String s = f.getName();
        if (s.contains(".")) {
            s = s.substring(0, s.indexOf("."));
        }
        return s;
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

    public static void launchJar(ProcessItem pro, File jar) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (os.contains("win")) {
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
                (new Thread(new Project.OutputReader(start.getInputStream(), pro.getConsole()))).start();
                (new Thread(new Project.ErrorReader(start.getErrorStream(), pro.getConsole()))).start();
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

    private static String getJavaHomeLocation() {
        return Dependencies.localVersionProperty.get();
    }

}
