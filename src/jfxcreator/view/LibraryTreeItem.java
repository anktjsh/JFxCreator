/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

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

/**
 *
 * @author swatijoshi
 */
public class LibraryTreeItem extends TreeItem<String> {

    private static final Image lib = new Image(LibraryTreeItem.class.getResourceAsStream("tree/library.PNG"), 25, 25, true, true);
    private final String path;

    public LibraryTreeItem(String a) {
        path = a;
        File f = new File(path);
        setValue(f.getName());
        setGraphic(new ImageView(lib));
        try {
            ZipFile zf = new ZipFile(f);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry next = entries.nextElement();
                if (next.getName().endsWith(File.separator) && next.getName().substring(0, next.getName().length() - 1).contains(File.separator)) {
                    add(next.getName());
                } else if (!next.getName().endsWith(File.separator) && next.getName().contains(File.separator)) {
                    add(next.getName());
                } else {
                    getChildren().add(new TreeItem<>(replaceAll(next.getName(), File.separator, "")));
                }
            }
        } catch (Exception e) {
        }
    }

    private void add(String s) {
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
            ad.getChildren().add(new TreeItem<>(last));
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
