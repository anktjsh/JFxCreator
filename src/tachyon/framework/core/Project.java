/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.framework.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import tachyon.framework.manager.TaskManager;
import tachyon.java.core.DebuggerController;
import tachyon.process.ProcessItem;

/**
 *
 * @author Aniket
 */
public abstract class Project {

    private final Path rootDirectory;
    private final Path config;
    private final Path source, dist;
    private final TaskManager manager;
    private final String projectName;
    private final ArrayList<Program> programs;
    private final Task<Void> task;
    private final ObservableList<ProjectListener> listeners;

    public Project(Path rot, boolean isNew) {
        rootDirectory = rot;
        projectName = rootDirectory.getFileName().toString();
        if (!Files.exists(rootDirectory)) {
            try {
                Files.createDirectories(rootDirectory);
            } catch (IOException ex) {
            }
        }
        source = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "src");
        dist = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "dist");
        if (!Files.exists(source)) {
            try {
                Files.createDirectories(source);
            } catch (IOException ex) {
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
        config = Paths.get(rootDirectory.toAbsolutePath().toString() + File.separator + "settings.tachyon");
        (new Thread(task = new FileWatcher())).start();
        manager = constructManager();
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
                        } else if (kind == ENTRY_CREATE) {

                        } else if (kind == ENTRY_MODIFY) {

                        } else if (kind == ENTRY_DELETE) {

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

    public TaskManager getTaskManager() {
        return manager;
    }

    public Path getConfig() {
        return config;
    }

    protected boolean addProgram(Program pro) {
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
            for (ProjectListener pl : listeners) {
                pl.fileAdded(this, scr);
            }
        }
    }

    public void removeScript(Program scr) {
        programs.remove(scr);
        for (ProjectListener pl : listeners) {
            pl.fileRemoved(this, scr);
        }
    }

    public ArrayList<Program> getPrograms() {
        return programs;
    }

    public void addProjectListener(ProjectListener pl) {
        listeners.add(pl);
    }

    public void removeProjectListener(ProjectListener pr) {
        listeners.remove(pr);
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

    public void clean() {
        almostDeepDelete(dist.toFile());
    }

    public abstract String serialize();

    public static Project unserialize(String s) {
        try {
            String[] split = s.split(" : ");
            return unserialize(Paths.get(split[1]));
        } catch (Exception e) {
            return null;
        }
    }

    public static Project unserialize(Path f) {
        return loadProject(f);
    }

    public static Project loadProject(Path p) {
        Path config = Paths.get(p.toAbsolutePath().toString() + File.separator + "settings.tachyon");
        if (Files.exists(config)) {
            try {
                ArrayList<String> al = new ArrayList<>();
                al.addAll(Files.readAllLines(config));
                if (al.size() > 0) {
                    String type = getType(al.get(al.size() - 1));
                    Constructor c;
                    try {
                        int n = (c = (Class.forName(type)).getConstructors()[0]).getParameterCount();
                        int diff = n - 2;
                        if (al.size() >= diff + 1) {
                            Object[] ob = getObjectArray(p, false, al.subList(0, diff));
                            Project pa = (Project) c.newInstance(ob);
                            return pa;
                        }
                    } catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static Object[] getObjectArray(Path p, boolean b, List<String> al) {
        Object[] o = new Object[al.size() + 2];
        o[0] = p;
        o[1] = b;
        for (int x = 0; x < al.size(); x++) {
            o[x + 2] = al.get(x);
        }
        return o;
    }

    private static String getType(String s) {
        String temp = s.substring(1, s.length() - 1);
        String spl[] = temp.split(", ");
        byte[] b = new byte[spl.length];
        for (int x = 0; x < spl.length; x++) {
            b[x] = Byte.parseByte(spl[x]);
        }
        return new String(b);
    }

    protected abstract TaskManager constructManager();

    protected abstract void checkAll();

    protected abstract void initializeProject();

    protected abstract void readConfig();

    protected abstract void saveConfig();

    protected abstract void addExistingPrograms();

    public void compile(ProcessItem pro) {
        getTaskManager().compile(pro);
    }

    public void build(ProcessItem pro) {
        getTaskManager().build(pro);
    }

    public void run(ProcessItem pro) {
        getTaskManager().run(pro);
    }

    public void nativeExecutable(ProcessItem pro) {
        getTaskManager().nativeExecutable(pro);
    }

    public void debugProject(ProcessItem pro, DebuggerController con) {
        getTaskManager().debugProject(pro, con);
    }

    public void runIndividualFile(ProcessItem pro, Program program) {
        getTaskManager().runIndividualFile(pro, program);
    }

    public void fatBuild(ProcessItem pro) {
        getTaskManager().fatBuild(pro);
    }

    public interface ProjectListener {

        public void fileAdded(Project pro, Program add);

        public void fileRemoved(Project pro, Program scr);

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
}
