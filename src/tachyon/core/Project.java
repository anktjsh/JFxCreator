/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.core;

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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import tachyon.view.FileWizard;
import tachyon.view.LibraryTreeItem.LibraryListener;

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
    private final ObservableList<JavaLibrary> allLibs;
    private final HashMap<String, String> compileArguments;
    private final ArrayList<String> runtimeArguments;
    private final TaskManager manager;
    private String mainClassName;
    private String iconFilePath;

    public Project(Path src, String mcn, boolean isNew, int... conf) {
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
        compileArguments = new HashMap<>();
        runtimeArguments = new ArrayList<>();

        programs = new ArrayList<>();
        listeners = FXCollections.observableArrayList();
        if (isNew) {
            initializeProject(conf[0]);
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
        manager = new TaskManager(this);
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
            if (!programs.contains(pro)) {
                if (!Files.isHidden(pro.getFile())) {
                    programs.add(pro);
                    return true;
                }
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

    public HashMap<String, String> getCompileTimeArguments() {
        return compileArguments;
    }

    public ArrayList<String> getRuntimeArguments() {
        return runtimeArguments;
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

    private void saveConfig() {
        try {
            Files.write(config,
                    FXCollections.observableArrayList(mainClassName,
                            "Libs : " + allLibs,
                            compileArguments.toString(),
                            runtimeArguments.toString(),
                            iconFilePath == null ? "" : iconFilePath
                    ));
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

    public String serialize() {
        return "Project : " + getRootDirectory().toAbsolutePath().toString() + " : " + getMainClassName();
    }

    private void initializeProject(int check) {
        Program pro = null;
        if (check == 0) {
            pro = new Program(Program.JAVA,
                    Paths.get(source.toAbsolutePath() + Program.getFilePath(mainClassName) + ".java"),
                    FileWizard.getTemplateCode("Java Main Class", mainClassName),
                    this);
        } else if (check == 1) {
            pro = new Program(Program.JAVA,
                    Paths.get(source.toAbsolutePath() + Program.getFilePath(mainClassName) + ".java"),
                    FileWizard.getTemplateCode("JavaFx Main Class", mainClassName),
                    this);
        }
        if (pro != null) {
            addScript(pro);
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
        manager.runFile(item, program);
    }

    public void fatJar(ProcessItem pro) throws IOException {
        manager.fatJar(pro);
    }

    public String getFileList() {
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
        manager.compile(pro);
    }

    public void build(ProcessItem pro) {
        manager.build(pro);
    }

    public void run(ProcessItem pro) {
        manager.run(pro);
    }

    public void nativeExecutable(ProcessItem pro) {
        manager.nativeExecutable(pro);
    }

    public void debugProject(ProcessItem pro, DebuggerController con) {
        manager.debugProject(pro, con);
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

    public static class OutputReader implements Runnable {

        private final InputStream strea;
        private final Console console;

        public OutputReader(InputStream is, Console so) {
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
                    if (console == null) {
                        System.out.println(c);
                    } else {
                        console.log(c);
                    }
                }
            } catch (IOException ex) {
            }
        }
    }

    public static class ErrorReader implements Runnable {

        private final InputStream strea;
        private final Console console;

        public ErrorReader(InputStream is, Console so) {
            strea = is;
            console = so;
        }

        @Override
        public void run() {
            Scanner in = new Scanner(strea);
            while (in.hasNextLine()) {
                if (console == null) {
                    System.out.println(in.nextLine());
                } else {
                    console.error("\n" + in.nextLine());
                }
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

    @Override
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
