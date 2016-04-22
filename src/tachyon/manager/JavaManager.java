/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.manager;

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
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import tachyon.core.DebuggerController;
import tachyon.core.JavaLibrary;
import tachyon.core.JavaProgram;
import tachyon.core.JavaProject;
import tachyon.core.Program;
import tachyon.core.Project;
import tachyon.process.ProcessItem;
import tachyon.process.ProcessPool;

/**
 *
 * @author Aniket
 */
public class JavaManager extends ParentManager {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public JavaManager(JavaProject project) {
        super(project);
    }

    @Override
    public JavaProject getProject() {
        return (JavaProject) super.getProject();
    }

    @Override
    public void compile(ProcessItem pro) {
        if (!pro.isCancelled()) {
            ProcessBuilder pb = getCompileString();
            pb.directory(getProject().getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Compile Files for Project " + getProject().getProjectName());
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

    private ProcessBuilder getCompileString() {
        String one;
        if (OS.contains("win")) {
            one = "\"" + getJavaHomeLocation() + File.separator + "javac\""
                    + getProject().getFileList() + " -d "
                    + getProject().getBuild().toAbsolutePath().toString()
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()))
                    + getProject().getCompileList();
        } else {
            one = getJavaHomeLocation() + File.separator + "javac"
                    + getProject().getFileList()
                    + " -d "
                    + getProject().getBuild().toAbsolutePath().toString()
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()))
                    + getProject().getCompileList();
        }
        return new ProcessBuilder(one.split(" "));
    }

    @Override
    public void build(ProcessItem pro) {
        compile(pro);
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsBuildString();
            } else {
                pb = getMacBuildString();
            }
            pb.directory(getProject().getRootDirectory().toFile());

            try {
                Process start = pb.start();
                pro.setName("Build Jar File for Project " + getProject().getProjectName());
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
        File f = new File(getJavaHomeLocation() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        String one;
        if (f.exists()) {
            one = "\"" + getJavaHomeLocation() + File.separator + "javapackager\"" + " -createjar -appclass " + getProject().getMainClassName()
                    + " -srcdir " + getProject().getBuild().toAbsolutePath().toString() + " -outdir "
                    + getProject().getDist().toAbsolutePath().toString() + " -outfile " + getProject().getProjectName() + ".jar"
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        } else {
            one = "\"" + getJavaHomeLocation() + File.separator + "javafxpackager\"" + " -createjar -appclass " + getProject().getMainClassName()
                    + " -srcdir " + getProject().getBuild().toAbsolutePath().toString() + " -outdir "
                    + getProject().getDist().toAbsolutePath().toString() + " -outfile " + getProject().getProjectName() + ".jar"
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        }
        return new ProcessBuilder(one.split(" "));
    }

    private ProcessBuilder getMacBuildString() {
        File f = new File(getJavaHomeLocation() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        String one;
        if (f.exists()) {
            one = getJavaHomeLocation() + File.separator + "javapackager" + " -createjar -appclass " + getProject().getMainClassName()
                    + " -srcdir " + getProject().getBuild().toAbsolutePath().toString() + " -outdir "
                    + getProject().getDist().toAbsolutePath().toString() + " -outfile " + getProject().getProjectName() + ".jar"
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        } else {
            one = getJavaHomeLocation() + File.separator + "javafxpackager" + " -createjar -appclass " + getProject().getMainClassName()
                    + " -srcdir " + getProject().getBuild().toAbsolutePath().toString() + " -outdir "
                    + getProject().getDist().toAbsolutePath().toString() + " -outfile " + getProject().getProjectName() + ".jar"
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        }
        return new ProcessBuilder(one.split(" "));
    }

    @Override
    public void run(ProcessItem pro) {
        build(pro);
        if (!pro.isCancelled()) {
            ProcessBuilder pb = getRunString();
            pb.directory(getProject().getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Launching Jar File for Project " + getProject().getProjectName());
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

    private ProcessBuilder getRunString() {
        String one;
        if (OS.contains("win")) {
            one = "\"" + getJavaHomeLocation()
                    + File.separator + "java\"" + " -jar " + getProject().getDist().getFileName().toString()
                    + File.separator + getProject().getProjectName() + ".jar" + (getProject().getRuntimeList())
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        } else {
            one = getJavaHomeLocation()
                    + File.separator + "java" + " -jar " + getProject().getDist().getFileName().toString()
                    + File.separator + getProject().getProjectName() + ".jar" + (getProject().getRuntimeList())
                    + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        }
        return new ProcessBuilder(one.split(" "));
    }

    @Override
    public void nativeExecutable(ProcessItem pro) {
        build(pro);
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsExecutableString();
            } else {
                pb = getMacExecutableString();
            }
            pb.directory(getProject().getDist().toFile());
            try {
                Process start = pb.start();
                pro.setName("Compile Native for Project " + getProject().getRootDirectory().getFileName().toString());
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
        File f = new File(getJavaHomeLocation() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        String a;
        if (f.exists()) {
            a = "\"" + getJavaHomeLocation() + File.separator + "javapackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
                    + ".jar " + " -appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " -v";
        } else {
            a = "\"" + getJavaHomeLocation() + File.separator + "javafxpackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
                    + ".jar " + " -appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " -v";
        }
        return new ProcessBuilder(a.split(" "));
    }

    private ProcessBuilder getMacExecutableString() {
        String ico = getIconPath();
        File f = new File(getJavaHomeLocation() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        String a;
        if (f.exists()) {
            a = getJavaHomeLocation() + File.separator + "javapackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
                    + ".jar " + "-appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " mac.CFBundleName=" + getProject().getProjectName() + " -v";
        } else {
            a = getJavaHomeLocation() + File.separator + "javafxpackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
                    + ".jar " + "-appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " mac.CFBundleName=" + getProject().getProjectName() + " -v";
        }
        return new ProcessBuilder(Arrays.asList(a.split(" ")));
    }

    private String getIconPath() {
        if (getProject().getFileIconPath().isEmpty()) {
            return null;
        }
        if (OS.contains("win")) {
            if (getProject().getFileIconPath().endsWith(".ico")) {
                return getProject().getFileIconPath();
            } else {
                File f = new File(getProject().getFileIconPath());
                if (f.exists()) {
                    File to = new File(getProject().getDist().toAbsolutePath().toString() + File.separator + getFilename(f) + ".ico");
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
        } else if (getProject().getFileIconPath().endsWith(".icns")) {
            return getProject().getFileIconPath();
        } else {
            File f = new File(getProject().getFileIconPath());
            if (f.exists()) {
                File to = new File(getProject().getDist().toAbsolutePath().toString() + File.separator + getFilename(f) + ".icns");
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
        return null;
    }

    private String getFilename(File f) {
        String s = f.getName();
        if (s.contains(".")) {
            s = s.substring(0, s.indexOf("."));
        }
        return s;
    }

    private void buildFatJar(ProcessItem item) throws IOException {

        Path fat = Paths.get(getProject().getRootDirectory().toAbsolutePath().toString() + File.separator + "bundle" + File.separator + getProject().getProjectName() + ".jar");
        if (!Files.exists(fat.getParent())) {
            try {
                Files.createDirectories(fat.getParent());
            } catch (IOException ex) {
            }
        }
        almostDeepDelete(new File(fat.getParent().toAbsolutePath().toString()));
        String input = getProject().getDist().toAbsolutePath().toString() + File.separator + getProject().getProjectName() + ".jar";
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

        for (JavaLibrary lib : getProject().getAllLibs()) {
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
        Path mani = Paths.get(getProject().getRootDirectory().toAbsolutePath().toString() + File.separator + "bundle" + File.separator + "META-INF" + File.separator
                + "MANIFEST.MF");
        if (Files.exists(mani)) {
            Files.delete(mani);
        }
        buildFat(item);
        deepDelete(fat.getParent());
    }

    @Override
    public void fatBuild(ProcessItem item) {
        build(item);
        try {
            buildFatJar(item);
        } catch (IOException ex) {
        }
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
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsFatJarString();
            } else {
                pb = getMacFatJarString();
            }
            pb.directory(getProject().getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Combining All Existing Jars for Project " + getProject().getProjectName());
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
        File f = new File(getJavaHomeLocation() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        if (f.exists()) {
            return new ProcessBuilder("\"" + getJavaHomeLocation() + File.separator + "javapackager\"",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        } else {
            return new ProcessBuilder("\"" + getJavaHomeLocation() + File.separator + "javafxpackager\"",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        }
    }

    private ProcessBuilder getMacFatJarString() {
        File f = new File(getJavaHomeLocation() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        if (f.exists()) {
            return new ProcessBuilder(getJavaHomeLocation() + File.separator + "javapackager",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        } else {
            return new ProcessBuilder(getJavaHomeLocation() + File.separator + "javafxpackager",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        }
    }

    @Override
    public void debugProject(ProcessItem pro, DebuggerController controller) {
        compile(pro);
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsDebuggerString();
            } else {
                pb = getMacDebuggerString();
            }
            pb.directory(getProject().getBuild().toFile());
            try {
                Process start = pb.start();
                pro.setName("Debug Project " + getProject().getRootDirectory().getFileName().toString());
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
        String s = "\"" + getJavaHomeLocation() + File.separator + "jdb\" " + getProject().getMainClassName()
                + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        return new ProcessBuilder(s.split(" "));
    }

    private ProcessBuilder getMacDebuggerString() {
        String s = getJavaHomeLocation() + File.separator + "jdb " + getProject().getMainClassName()
                + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList()));
        return new ProcessBuilder(s.split(" "));
    }

    @Override
    public void runIndividualFile(ProcessItem item, Program pro) {
        if (pro instanceof JavaProgram) {
            JavaProgram program = (JavaProgram) pro;
            compile(item);
            if (!item.isCancelled()) {
                ProcessBuilder pb;
                if (OS.contains("win")) {
                    pb = getWindowsRunFileString(program);
                } else {
                    pb = getMacRunFileString(program);
                }
                pb.directory(getProject().getBuild().toFile());
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
    }

    private ProcessBuilder getWindowsRunFileString(JavaProgram program) {
        return new ProcessBuilder("\"" + getJavaHomeLocation() + File.separator + "java\"", program.getClassName()
                + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList())));
    }

    private ProcessBuilder getMacRunFileString(JavaProgram program) {
        return new ProcessBuilder(getJavaHomeLocation() + File.separator + "java", program.getClassName()
                + (getProject().getAllLibs().isEmpty() ? "" : (" -classpath" + getProject().getLibsList())));
    }

}
