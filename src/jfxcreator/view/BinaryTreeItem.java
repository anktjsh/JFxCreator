/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.scene.control.TreeItem;
import jfxcreator.core.Project;

/**
 *
 * @author swatijoshi
 */
public class BinaryTreeItem extends TreeItem<String> {

    private final ZipEntry entry;
    private final Project project;
    private final ZipFile file;

    public BinaryTreeItem(Project proj, String val, ZipFile zf, ZipEntry entry) {
        super(val);
        this.entry = entry;
        project = proj;
        file = zf;
    }

    public InputStream getInputStream() {
        try {
            return file.getInputStream(entry);
        } catch (IOException e) {
        }
        return null;
    }

    public Project getProject() {
        return project;
    }

    public ZipEntry getEntry() {
        return entry;
    }
}
