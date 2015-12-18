/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import jfxcreator.core.ProcessPool.ProcessItem;
import jfxcreator.view.Dependencies;
import jfxcreator.view.ProjectProperties;

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
    private final Task<Void> task;
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

                        if (Files.isDirectory(child) || !child.getFileName().toString().contains(".")) {
                            if (kind == ENTRY_DELETE) {
                                deleteInside(child);
                            } else {
                                registerAll(child, watch);
                                checkInside(child);
                            }
                            continue;
                        }
                        if (kind == ENTRY_CREATE) {
                            boolean already = false;
                            for (Program scr : programs) {
                                if (scr.getFile().equals(child)) {
                                    already = true;
                                }
                            }
                            if (!already) {
                                addScriptsToList(child);
                            }
                            checkSources();
                        } else if (kind == ENTRY_MODIFY) {
                            boolean already = false;
                            Program scra = null;
                            for (Program scr : programs) {
                                if (scr.getFile().equals(child)) {
                                    already = true;
                                    scra = scr;
                                }
                            }
                            if (!already) {
                                addScriptsToList(child);
                            } else {
                                reloadScript(child, scra);
                            }
                            checkSources();
                        } else if (kind == ENTRY_DELETE) {
                            Program scra = null;
                            for (Program scr : programs) {
                                if (scr.getFile().equals(child)) {
                                    scra = scr;
                                }
                            }
                            if (scra != null) {
                                removeScript(scra);
                            }
                            checkSources();
                        }

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

    private void checkSources() {
        ArrayList<Path> al = new ArrayList<>();
        getAllSources(al, source);
        ArrayList<Program> remove = new ArrayList<>();
        ArrayList<Path> add = new ArrayList<>();
        programs.stream().filter((scr) -> (!al.contains(scr.getFile()))).forEach((scr) -> {
            remove.add(scr);
        });
        al.stream().forEach((P) -> {
            Path found = null;
            for (Program scr : programs) {
                if (scr.getFile().equals(P)) {
                    found = P;
                }
            }
            if (found == null) {
                add.add(P);
            }
        });
        remove.stream().forEach((scr) -> {
            removeScript(scr);
        });
        add.stream().forEach((p) -> {
            addScriptsToList(p);
        });
    }

    private ArrayList<Path> getAllSources(ArrayList<Path> al, Path p) {
        if (Files.isDirectory(p) || !p.getFileName().toString().contains(".")) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
                for (Path file : stream) {
                    getAllSources(al, file);
                }
            } catch (IOException | DirectoryIteratorException x) {
            }
            return al;
        } else {
            al.add(p);
            return al;
        }
    }

    private void reloadScript(Path child, Program scr) {
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(child));
        } catch (IOException ex) {
        }
        if (!al.equals(scr.getLastCode())) {
            removeScript(scr);
            scr.reload();
            addScript(scr);
        }
    }

    private void deleteInside(Path dir) {
        if (Files.isDirectory(dir) || !dir.getFileName().toString().contains(".")) {
            for (int x = programs.size() - 1; x >= 0; x--) {
                if (programs.get(x).getFile().toAbsolutePath().toString().contains(dir.toAbsolutePath().toString())) {
                    removeScript(programs.get(x));
                }
            }
        } else {
            Program scra = null;
            for (Program scr : programs) {
                if (scr.getFile().equals(dir)) {
                    scra = scr;
                }
            }
            if (scra != null) {
                removeScript(scra);
            }
        }
    }

    private void checkInside(Path dir) {
        if (Files.isDirectory(dir) || !dir.getFileName().toString().contains(".")) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    checkInside(file);
                }
            } catch (IOException | DirectoryIteratorException x) {
            }
        } else {
            boolean already = false;
            for (Program scr : programs) {
                if (scr.getFile().equals(dir)) {
                    already = true;
                }
            }
            if (!already) {
                addScriptsToList(dir);
            }

        }
    }

    private void addScriptsToList(Path f) {
        if (!Files.isDirectory(f) || f.getFileName().toString().contains(".")) {
            if (f.getFileName().toString().endsWith(".java")) {
                programs.add(new Program(Program.JAVA, f, new ArrayList<>(), this));
            } else {
                programs.add(new Program(Program.RESOURCE, f, new ArrayList<>(), this));
            }
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(f)) {
                for (Path fa : stream) {
                    addScriptsToList(fa);
                }
            } catch (IOException | DirectoryIteratorException x) {
            }
        }
    }

    public void addScript(Program scr) {
        programs.add(scr);
        listeners.stream().forEach((pl) -> {
            pl.fileAdded(this, scr);
        });
    }

    public void removeScript(Program scr) {
        programs.remove(scr);
        listeners.stream().forEach((pl) -> {
            pl.fileRemoved(this, scr);
        });
    }

    private void saveConfig() {
        try {
            Files.write(config,
                    FXCollections.observableArrayList(mainClassName));
        } catch (IOException e) {
        }
    }

    private void readConfig() {
        
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
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
            for (Path file : stream) {
                addScriptsToList(file);
            }
        } catch (IOException | DirectoryIteratorException x) {
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
        //
    }

    public void close() {
        if (task.isRunning()) {
            task.cancel();
        }
        saveConfig();
    }

    private String getFileList() {
        StringBuilder sb = new StringBuilder();
        for (Program p : programs) {
            sb.append(" ").append(p.getFile().toAbsolutePath().toString().endsWith(".java") ? p.getFile().toAbsolutePath().toString() : "");
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
                + build.toAbsolutePath().toString();
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
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macCompile(ProcessItem pro) {

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
                + dist.toAbsolutePath().toString() + " -outfile " + getProjectName() + ".jar";
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
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macBuild(ProcessItem pro) {

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
        String one = "\"" + JAVA_HOME + File.separator + "java\"" + " -jar " + dist.getFileName().toString() + File.separator + getProjectName() + ".jar";
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
        } catch (IOException | InterruptedException e) {
        }
    }

    private void macRun(ProcessItem pro) {

    }

    public void clean() {
        almostDeepDelete(build.toFile());
        almostDeepDelete(dist.toFile());
    }

    public void stop(ProcessItem pro) {

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
        if (Files.isDirectory(fe) || !fe.getFileName().toString().contains(".")) {
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
            int value = 0;
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
}
