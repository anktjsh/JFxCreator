/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Aniket
 */
public class JavaLibrary {

    private final Path lib;

    private final ArrayList<Path> sources;

    public JavaLibrary(String path, String... sour) {
        lib = Paths.get(path);
        sources = new ArrayList<>();
    }

    public void addSource(String s) {
        sources.add(Paths.get(s));
    }

    public Path getBinaryPath() {
        return lib;
    }

    public ArrayList<Path> getSources() {
        return sources;
    }

    public String getBinaryAbsolutePath() {
        return lib.toAbsolutePath().toString();
    }

    public String getSourceAbsolutePath(int x) {
        if (x < sources.size()) {
            return sources.get(x).toAbsolutePath().toString();
        }
        return "";
    }

    private ZipFile zip;

    public ZipFile getBinaryZipFile() {
        if (zip == null) {
            try {
                zip = new ZipFile(lib.toFile());
            } catch (IOException ex) {
            }
        }
        return zip;

    }

    public ZipFile getSourceZipFile(int x) {
        if (x < sources.size()) {
            try {
                return new ZipFile(sources.get(x).toFile());
            } catch (IOException ex) {
            }
        }
        return null;
    }

    public ZipInputStream getSourceZipInputStream(int x) {
        if (x < sources.size()) {
            try {
                return new ZipInputStream(new FileInputStream(sources.get(x).toFile()));
            } catch (FileNotFoundException ex) {
            }
        }
        return null;
    }

    public ZipInputStream getBinaryZipInputStream() {
        try {
            return new ZipInputStream(new FileInputStream(lib.toFile()));
        } catch (FileNotFoundException ex) {
        }
        return null;
    }

    @Override
    public String toString() {
        return lib.toAbsolutePath().toString();
    }

}
