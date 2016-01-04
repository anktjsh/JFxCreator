/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import jfxcreator.core.ProcessPool.ProcessItem;
import jfxcreator.view.Dependencies;
import jfxcreator.view.LibraryTreeItem.LibraryListener;

/**
 *
 * @author Aniket
 */
public class Project {

    private final Path rootDirectory;
    private final String projectName;
    private final Path source, libs, dist, build;
    private final Path config;
    private final ArrayList<Program> programs;
    private final ObservableList<ProjectListener> listeners;
    private LibraryListener ll;
    private final Task<Void> task;
    private final ObservableList<String> allLibs;
    private String mainClassName;

    public Project(Path src, String mcn, boolean isNew) {
        rootDirectory = src;
        if (!Files.exists(rootDirectory)) {
            try {
                Files.createDirectories(rootDirectory);
            } catch (IOException ex) {
            }
        }
        this.mainClassName = mcn;
        projectName = rootDirectory.getFileName().toString();
        source = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "src");
        libs = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "libs");
        build = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "build");
        dist = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "dist");
        if (!Files.exists(source)) {
            try {
                Files.createDirectories(source);
            } catch (IOException ex) {
            }
        }
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
        if (!Files.exists(dist)) {
            try {
                Files.createDirectories(dist);
            } catch (IOException ex) {
            }
        }

        allLibs = FXCollections.observableArrayList();

        programs = new ArrayList<>();
        listeners = FXCollections.observableArrayList();
        if (isNew) {
            initializeProject();
        } else {
            addExistingPrograms();
        }

        (new Thread(task = new FileWatcher())).start();
        config = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "settings.config");
        if (!Files.exists(config)) {
            saveConfig();
        } else {
            readConfig();
        }
    }

    public Path getConfig() {
        return config;
    }

    public static Project loadProject(Path pro, boolean isNew) {
        Path config = Paths.get(pro.toAbsolutePath().toString() + File.separator + "settings.config");
        if (Files.exists(config)) {
            String main;
            try {
                main = getMainClassFromConfig(config);
            } catch (IOException ex) {
                return null;
            }
            if (main == null) {
                return null;
            }
            return new Project(pro, main, isNew);
        }
        return null;
    }

    public static String getMainClassFromConfig(Path pa) throws IOException {
        List<String> al = Files.readAllLines(pa);
        if (al.isEmpty()) {
            return null;
        } else {
            return al.get(0);
        }
    }

    public LibraryListener getLibraryListener() {
        return ll;
    }

    public void setLibraryListener(LibraryListener ll) {
        this.ll = ll;
    }

    private class FileWatcher extends Task<Void> {

        private void registerAll(final Path start, WatchService watcher) throws IOException {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        @Override
        protected Void call() throws Exception {

            for (;;) {
                try {
                    if (isCancelled()) {
                        break;
                    }
                    WatchService watch = FileSystems.getDefault().newWatchService();
                    Path dir = source;
                    registerAll(dir, watch);
                    WatchKey key = watch.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (isCancelled()) {
                            break;
                        }
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        }
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();
                        Path child = dir.resolve(filename);
                        System.out.println(child.toAbsolutePath().toString());
                        System.out.println(kind.name());
                        if (Files.isDirectory(child)) {
                            registerAll(child, watch);
                        } else {
                            if (kind == ENTRY_CREATE) {

                            } else if (kind == ENTRY_MODIFY) {

                            } else if (kind == ENTRY_DELETE) {

                            }
                        }
                        checkAll();
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                    if (isCancelled()) {
                        break;
                    }
                } catch (IOException | InterruptedException ex) {
                }
            }
            return null;
        }
    }

    private void checkAll() {
        ArrayList<Program> remove = new ArrayList<>();
        ArrayList<Path> add = new ArrayList<>();
        addAllPaths(add, source.toFile());
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

    private void addAllPaths(ArrayList<Path> al, File p) {
        if (p.isDirectory()) {
            for (File f : p.listFiles()) {
                addAllPaths(al, f);
            }
        } else {
            al.add(p.toPath());
        }
    }

    private void addScriptsToList(File f, boolean b) {
        if (!f.isDirectory()) {
            if (f.getName().endsWith(".java")) {
                if (b) {
                    addScript(new Program(Program.JAVA, f.toPath(), new ArrayList<>(), this));
                } else {
                    addProgram(new Program(Program.JAVA, f.toPath(), new ArrayList<>(), this));
                }
            } else {
                if (b) {
                    addScript(new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), this));
                } else {
                    addProgram(new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), this));
                }
            }
        } else {
            for (File fa : f.listFiles()) {
                addScriptsToList(fa, b);
            }
        }
    }

    private boolean addProgram(Program pro) {
        try {
            if (!Files.isHidden(pro.getFile())) {
                programs.add(pro);
                return true;
            }
        } catch (IOException ex) {
        }
        return false;
    }

    public void addScript(Program scr) {
        if (addProgram(scr)) {
            listeners.stream().forEach((pl) -> {
                pl.fileAdded(this, scr);
            });
        }
    }

    public void removeScript(Program scr) {
        programs.remove(scr);
        listeners.stream().forEach((pl) -> {
            pl.fileRemoved(this, scr);
        });
    }

    public List<String> getAllLibs() {
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
        allLibs.addAll(all);
        if (ll != null) {
            ll.librariesChanged(all);
        }
        saveConfig();
    }

    private void saveConfig() {
        try {
            Files.write(config,
                    FXCollections.observableArrayList(mainClassName,
                            "Libs : " + allLibs));
        } catch (IOException e) {
        }
    }

    private void readConfig() {
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(config));
        } catch (IOException ex) {
        }
        if (al.size() >= 2) {
            String spl[] = al.get(1).split(" : ");
            String liberator = spl[1].substring(1, spl[1].length() - 1);
            allLibs.addAll(Arrays.asList(liberator.split(", ")));
            if (allLibs.size() == 1 && allLibs.get(0).isEmpty()) {
                allLibs.clear();
            }
        }
    }

    public String serialize() {
        return "Project : " + getRootDirectory().toAbsolutePath().toString() + " : " + getMainClassName();
    }

    private void initializeProject() {
        Program pro = new Program(Program.JAVA,
                Paths.get(source.toAbsolutePath() + Program.getFilePath(mainClassName) + ".java"),
                getInitialCode(mainClassName),
                this);
        addScript(pro);
    }

    private List<String> getInitialCode(String className) {
        if (className.contains(".")) {
            String pack = className.substring(0, className.lastIndexOf('.'));
            String clas = className.substring(className.lastIndexOf('.') + 1);
            return getInitialCode(pack, clas);
        } else {
            return getInitialCode(null, className);
        }
    }

    private List<String> getInitialCode(String packageName, String className) {
        if (packageName != null) {
            String list = "\n"
                    + "package " + packageName + ";\n"
                    + "\n"
                    + "public class " + className + " {\n"
                    + "    \n"
                    + "    public static void main (String args[]) {\n"
                    + "        System.out.println(\"Hello, World!\");\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public class " + className + " {\n"
                    + "    \n"
                    + "    public static void main (String args[]) {\n"
                    + "        System.out.println(\"Hello, World!\");\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    public ArrayList<Program> getPrograms() {
        return programs;
    }

    private void addExistingPrograms() {
        for (File f : source.toFile().listFiles()) {
            addScriptsToList(f, false);
        }
    }

    public void addProjectListener(ProjectListener pl) {
        listeners.add(pl);
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String main) {
        mainClassName = main;
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    public String getProjectName() {
        return projectName;
    }

    public Path getSource() {
        return source;
    }

    public Path getBuild() {
        return build;
    }

    public Path getLibs() {
        return libs;
    }

    public Path getDist() {
        return dist;
    }

    public void delete() {
        deepDelete(rootDirectory);
    }

    public void close() {
        if (task.isRunning()) {
            task.cancel();
        }
        saveConfig();
    }

    public static void uncompress(String compress, String folderPath) {
        int BUFFER = 2048;
        System.out.println(System.currentTimeMillis());
        try {
            String uncompress;
            BufferedOutputStream dest;
            FileInputStream fis = new FileInputStream(compress);
            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    uncompress = folderPath + entry.getName();
                    System.out.println("Extracting entry");
                    if (entry.isDirectory()) {
                        File f = new File(uncompress);
                        f.mkdir();
                    } else {
                        int count;
                        byte[] data = new byte[BUFFER];

                        FileOutputStream fos = new FileOutputStream(new File(uncompress));
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                    }

                }
            }
        } catch (IOException ex) {
        }
        System.out.println(System.currentTimeMillis());
    }

    public void runFile(ProcessItem item, Program program) {
        String OS = System.getProperty("os.name").toLowerCase();
        compile(item);
        if (OS.contains("win")) {
            windowsRunFile(item, program);
        } else {
            macRunFile(item, program);
        }
    }

    private void windowsRunFile(ProcessItem item, Program program) {
        String JAVA_HOME = Dependencies.local_version;
        ProcessBuilder pb = new ProcessBuilder("\"" + JAVA_HOME + File.separator + "java\"", program.getClassName());
        pb.directory(build.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            item.setName("Launching File : " + program.getClassName());
            item.setProcess(start);
            ProcessPool.getPool().addItem(item);
            (new Thread(new Reader(start.getInputStream(), item.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macRunFile(ProcessItem item, Program program) {
        String JAVA_HOME = Dependencies.local_version;
        ProcessBuilder pb = new ProcessBuilder(JAVA_HOME + File.separator + "java", program.getClassName());
        pb.directory(build.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            item.setName("Launching File : " + program.getClassName());
            item.setProcess(start);
            ProcessPool.getPool().addItem(item);
            (new Thread(new Reader(start.getInputStream(), item.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    public void fatJar(ProcessItem pro) throws IOException {
        build(pro);
        Path fat = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "bundle" + File.separator + getProjectName() + ".jar");
        if (!Files.exists(fat.getParent())) {
            try {
                Files.createDirectories(fat.getParent());
            } catch (IOException ex) {
            }
        }
        almostDeepDelete(new File(fat.getParent().toAbsolutePath().toString()));
        String input = dist.toAbsolutePath().toString() + File.separator + getProjectName() + ".jar";
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

        for (String init : getAllLibs()) {
            try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(init))) {
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
        Path mani = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "bundle" + File.separator + "META-INF" + File.separator
                + "MANIFEST.MF");
        if (Files.exists(mani)) {
            Files.delete(mani);
        }
        buildFat(pro);
        deepDelete(fat.getParent());
    }

    private void buildFat(ProcessItem pro) {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            windowsFat(pro);
        } else {
            macFat(pro);
        }
    }

    private void windowsFat(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        ProcessBuilder pb = new ProcessBuilder("\"" + JAVA_HOME + File.separator + "javapackager\"",
                "-createjar",
                "-appClass", getMainClassName(),
                "-srcdir", "bundle",
                "-outdir", "dist",
                "-outfile", "bundle.jar", "-v");
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Combining All Existing Jars for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macFat(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        ProcessBuilder pb = new ProcessBuilder(JAVA_HOME + File.separator + "javapackager",
                "-createjar",
                "-appClass", getMainClassName(),
                "-srcdir", "bundle",
                "-outdir", "dist",
                "-outfile", "bundle.jar", "-v");
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Combining All Existing Jars for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
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

    private String getFileList() {
        StringBuilder sb = new StringBuilder();
        programs.stream().forEach((p) -> {
            if (p.getFile().toAbsolutePath().toString().endsWith(".java")) {
                sb.append(" ").append(p.getFile().toAbsolutePath().toString());
            }
        });
        return sb.toString();
    }

    public int getNumLibs() {
        return allLibs.size();
    }

    public String getLibsList() {
        StringBuilder sb = new StringBuilder();
        File f = new File(rootDirectory.toAbsolutePath().toString() + File.separator + "libs");
        for (File file : f.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                sb.append(" ").append(file.getAbsolutePath());
            }
        }
        return sb.toString();
    }

    public void compile(ProcessItem pro) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            windowsCompile(pro);
        } else {
            macCompile(pro);
        }
    }

    private void windowsCompile(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String one = "\"" + JAVA_HOME + File.separator + "javac\""
                + getFileList() + " -d "
                + build.toAbsolutePath().toString()
                + (getAllLibs().isEmpty() ? "" : (" -classpath" + getLibsList()));
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Compile Files for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macCompile(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String one = JAVA_HOME + File.separator + "javac"
                + getFileList()
                + " -d "
                + build.toAbsolutePath().toString()
                + (getAllLibs().isEmpty() ? "" : (" -classpath" + getLibsList()));
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Compile Files for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            System.out.println(start.waitFor());
        } catch (IOException | InterruptedException e) {

        }
    }

    public void build(ProcessItem pro) {
        compile(pro);
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            windowsBuild(pro);
        } else {
            macBuild(pro);
        }
    }

    private void windowsBuild(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String one = "\"" + JAVA_HOME + File.separator + "javapackager\"" + " -createjar -appclass " + getMainClassName()
                + " -srcdir " + build.toAbsolutePath().toString() + " -outdir "
                + dist.toAbsolutePath().toString() + " -outfile " + getProjectName() + ".jar" + " -classpath" + getLibsList();
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Build Jar File for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macBuild(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String one = JAVA_HOME + File.separator + "javapackager" + " -createjar -appclass " + getMainClassName()
                + " -srcdir " + build.toAbsolutePath().toString() + " -outdir "
                + dist.toAbsolutePath().toString() + " -outfile " + getProjectName() + ".jar" + " -classpath" + getLibsList();
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Build Jar File for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    public void run(ProcessItem pro) {
        build(pro);
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            windowsRun(pro);
        } else {
            macRun(pro);
        }
    }

    private void windowsRun(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String one = "\"" + JAVA_HOME
                + File.separator + "java\"" + " -jar " + dist.getFileName().toString()
                + File.separator + getProjectName() + ".jar"
                + " -classpath" + getLibsList();
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        pb.environment().put("PATH", JAVA_HOME);
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Launching Jar File for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macRun(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String one = JAVA_HOME
                + File.separator + "java" + " -jar " + dist.getFileName().toString()
                + File.separator + getProjectName() + ".jar"
                + " -classpath" + getLibsList();
        ProcessBuilder pb = new ProcessBuilder(one.split(" "));
        pb.environment().put("PATH", JAVA_HOME);
        pb.directory(rootDirectory.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Launching Jar File for Project " + getProjectName());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
        }
    }

    public void nativeExecutable(ProcessItem pro) {
        build(pro);
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            windowsExecutable(pro);
        } else {
            macExecutable(pro);
        }
    }

    private void windowsExecutable(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String ico = null;
        String a = "\"" + JAVA_HOME + File.separator + "javapackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + dist.toAbsolutePath().toString()
                + " -outfile " + getProjectName() + " -srcdir " + dist.toAbsolutePath().toString() + " -srcFiles " + getProjectName()
                + ".jar " + " -appclass " + getMainClassName() + " -name " + getProjectName() + " -title " + getProjectName() + " -v";
        ProcessBuilder pb = new ProcessBuilder(a.split(" "));
        pb.directory(dist.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Compile Native .exe for Project " + getRootDirectory().getFileName().toString());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
        } catch (IOException | InterruptedException ex) {
        }
    }

    private void macExecutable(ProcessItem pro) {
        String JAVA_HOME = Dependencies.local_version;
        String ico = null;
        ProcessBuilder pb;
        String a = JAVA_HOME + File.separator + "javapackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + dist.toAbsolutePath().toString()
                + " -outfile " + getProjectName() + " -srcdir " + dist.toAbsolutePath().toString() + " -srcFiles " + getProjectName()
                + ".jar " + "-appclass " + getMainClassName() + " -name " + getProjectName() + " -title " + getProjectName() + " mac.CFBundleName=" + getProjectName() + " -v";
        pb = new ProcessBuilder(Arrays.asList(a.split(" ")));
        pb.directory(dist.toFile());
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            pro.setName("Compile Native for Project " + getRootDirectory().getFileName().toString());
            pro.setProcess(start);
            ProcessPool.getPool().addItem(pro);
            (new Thread(new Reader(start.getInputStream(), pro.getConsole()))).start();
            int waitFor = start.waitFor();
        } catch (IOException | InterruptedException ex) {
        }
    }

    public void clean() {
        almostDeepDelete(build.toFile());
        almostDeepDelete(dist.toFile());
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

    public interface ProjectListener {

        public void fileAdded(Project pro, Program add);

        public void fileRemoved(Project pro, Program scr);

    }

    public static class Reader implements Runnable {

        private final InputStream strea;
        private final Console console;

        public Reader(InputStream is, Console so) {
            strea = is;
            console = so;
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(strea);
            BufferedReader br = new BufferedReader(isr);
            int value;
            try {
                while ((value = br.read()) != -1) {
                    char c = (char) value;
                    console.log(c);
                }
            } catch (IOException ex) {
            }
        }
    }

    public static Project unserialize(String s) {
        try {
            String[] split = s.split(" : ");
            return unserialize(Paths.get(split[1]));
        } catch (Exception e) {
            return null;
        }
    }

    public static Project unserialize(Path f) {
        return loadProject(f, false);
    }

    public void addListener(ProjectListener al) {
        listeners.add(al);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            Project pro = (Project) obj;
            if (pro.getRootDirectory().equals(getRootDirectory())) {
                return true;
            }
        }
        return false;
    }
}
