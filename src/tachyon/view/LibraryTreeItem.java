/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tachyon.java.core.JavaLibrary;
import tachyon.framework.core.Project;

/**
 *
 * @author Aniket
 */
public class LibraryTreeItem extends TreeItem<String> {

    private static final Image lib = new Image(LibraryTreeItem.class.getResourceAsStream("tree/library.PNG"), 25, 25, true, true);
    private final JavaLibrary library;
    private final Project project;

    public LibraryTreeItem(Project pro, JavaLibrary a) {
        library = a;
        project = pro;
        setValue(a.getBinaryPath().getFileName().toString());
        setGraphic(new ImageView(lib));
        try {
            ZipFile zf = a.getBinaryZipFile();
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry next = entries.nextElement();
                if (next.isDirectory()) {
                    continue;
                }
                if (next.getName().endsWith("\\") && next.getName().substring(0, next.getName().length() - 1).contains("\\")) {
                    add(next.getName(), zf, next);
                } else if (!next.getName().endsWith("\\") && next.getName().contains("\\")) {
                    add(next.getName(), zf, next);
                } else {
                    getChildren().add(new BinaryTreeItem(project, replaceAll(next.getName(), "\\", ""), library, next));
                }
            }
        } catch (Exception e) {
        }
    }

    public Project getProject() {
        return project;
    }

    private void add(String s, ZipFile zf, ZipEntry entry) {
        String spl[] = s.split(File.separator);
        ArrayList<String> al = new ArrayList<>();
        al.addAll(Arrays.asList(spl));
        String last = al.remove(al.size() - 1);
        TreeItem<String> ad = null;
        while (!al.isEmpty()) {
            if (ad == null) {
                for (TreeItem<String> as : getChildren()) {
                    if (as.getValue().equals(al.get(0))) {
                        al.remove(0);
                        ad = as;
                    }
                }

            } else {
                for (TreeItem<String> as : ad.getChildren()) {
                    if (as.getValue().equals(al.get(0))) {
                        al.remove(0);
                        ad = as;
                    }
                }
            }
        }
        if (ad != null) {
            ad.getChildren().add(new BinaryTreeItem(project, last, library, entry));
        }
    }

    private String replaceAll(String txt, String init, String repl) {
        while (txt.contains(init)) {
            int index = txt.indexOf(init);
            String first = txt.substring(0, index);
            String two = txt.substring(index + 1);
            txt = first + repl + two;
        }
        return txt;
    }

    public static interface LibraryListener {

        public void librariesChanged(List<String> filePaths);
    }
}
