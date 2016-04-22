/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.utility;

/**
 *
 * @author Aniket
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private final List<String> fileList;

    public ZipUtils() {
        fileList = new ArrayList<>();
    }

    public void zipIt(String SOURCE_FOLDER, String zipFile) {
        byte[] buffer = new byte[1024];
        String source;
        FileOutputStream fos;
        ZipOutputStream zos = null;
        try {
            try {
                source = SOURCE_FOLDER.substring(SOURCE_FOLDER.lastIndexOf('\\') + 1, SOURCE_FOLDER.length());
            } catch (Exception e) {
                source = SOURCE_FOLDER;
            }
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            FileInputStream in = null;
            for (String file : fileList) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }
            zos.closeEntry();
        } catch (IOException ex) {
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
            }
        }
    }

    public void generateFileList(File node, String SOURCE_FOLDER) {
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), SOURCE_FOLDER));
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename), SOURCE_FOLDER);
            }
        }
    }

    private String generateZipEntry(String file, String SOURCE_FOLDER) {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }
}
