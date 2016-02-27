/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.scene.control.TreeItem;
import tachyon.core.JavaLibrary;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class BinaryTreeItem extends TreeItem<String> {

    private final ZipEntry entry;
    private final Project project;
    private final JavaLibrary lib;

    public BinaryTreeItem(Project proj, String val, JavaLibrary lisb, ZipEntry entry) {
        super(val);
        this.entry = entry;
        project = proj;
        lib = lisb;
    }

    public InputStream getInputStream() {
        try {
            return lib.getBinaryZipFile().getInputStream(entry);
        } catch (IOException e) {
        }
        return null;
    }

    public InputStream sourceInputStream() {
        if (entry.getName().endsWith(".class")) {
            int index;
            if (entry.getName().contains("$")) {
                index = entry.getName().lastIndexOf("$");
            } else {
                index = entry.getName().lastIndexOf(".class");
            }
            for (Path p : lib.getSources()) {
                try {
                    ZipFile op = new ZipFile(p.toFile());
                    ZipEntry ent = op.getEntry(entry.getName().substring(0, index) + ".java");
                    if (ent != null) {
                        return op.getInputStream(ent);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BinaryTreeItem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return getInputStream();
    }

    public Project getProject() {
        return project;
    }

    public ZipEntry getEntry() {
        return entry;
    }
}
