/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jfxcreator.view.Dependencies;

/**
 *
 * @author Aniket
 */
public class JavaPlatform {

    private static final ObjectProperty<JavaPlatform> currentPlatform = new SimpleObjectProperty<>();

    static {
        Dependencies.localVersionProperty.addListener((ob, older, newer) -> {
            if (newer != null) {
                currentPlatform.set(new JavaPlatform(newer));
            }
        });
        if (Dependencies.localVersionProperty.get() != null && !Dependencies.localVersionProperty.get().isEmpty()) {
            currentPlatform.set(new JavaPlatform(Dependencies.localVersionProperty.get()));
        }
    }

    public static ObjectProperty<JavaPlatform> currentPlatformProperty() {
        return currentPlatform;
    }

    public static JavaPlatform getCurrentPlatform() {
        return currentPlatform.get();
    }

    private final File rootDirectory;
    private final ArrayList<File> jarFiles;
    private final File sourceFolder, fxSourceFolder;
    private final ArrayList<JavaLibrary> allLibs;

    private JavaPlatform(String path) {
        rootDirectory = getPlatformFile(path);
        jarFiles = new ArrayList<>();
        addAllFiles(rootDirectory, jarFiles);
        sourceFolder = getSourceFile(path);
        fxSourceFolder = getFxSourceFile(path);
        allLibs = new ArrayList<>();
        for (File f : jarFiles) {
            allLibs.add(new JavaLibrary(f.getAbsolutePath()));
        }
        for (JavaLibrary jl : allLibs) {
            jl.addSource(sourceFolder.getAbsolutePath());
            jl.addSource(fxSourceFolder.getAbsolutePath());
        }
    }
    
    public ArrayList<JavaLibrary> getAllLibs() {
        return allLibs;
    }

    private File getSourceFile(String path) {
        File home = new File(path);
        return new File(home.getParent() + File.separator + "src.zip");
    }

    private File getFxSourceFile(String path) {
        File home = new File(path);
        return new File(home.getParent() + File.separator + "javafx-src.zip");
    }

    private File getPlatformFile(String path) {
        File home = new File(path);
        return new File(home.getParent() + File.separator + "jre" + File.separator + "lib");
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    private void addAllFiles(File f, ArrayList<File> jars) {
        if (f.exists()) {
            if (f.isDirectory()) {
                if (f.getName().equals("security")) {
                    return;
                }
                for (File fa : f.listFiles()) {
                    addAllFiles(fa, jars);
                }
            } else {
                if (f.getName().endsWith(".jar")) {
                    if (f.getName().equals("deploy.jar")
                            || f.getName().equals("javaws.jar")
                            || f.getName().equals("management-agent.jar")
                            || f.getName().equals("plugin.jar")) {
                        return;
                    }
                    jars.add(f);
                }
            }
        }
    }

    public InputStream getInputStream(String className) {
        try {
            String path = (getEntryPath(className) + ".java");
            ZipFile zf = new ZipFile(sourceFolder);
            ZipEntry entry = zf.getEntry(path);
            if (entry != null) {
                return zf.getInputStream(entry);
            }
            ZipFile zf2 = new ZipFile(fxSourceFolder);
            ZipEntry ze = zf2.getEntry(path);
            if (ze != null) {
                return zf2.getInputStream(ze);
            }
        } catch (IOException ex) {
            Logger.getLogger(JavaPlatform.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String getEntryPath(String className) {
        while (className.contains(".")) {
            String one = className.substring(0, className.indexOf('.'));
            String two = className.substring(className.indexOf('.') + 1);
            className = one + '/' + two;
        }
        return className;
    }
}
