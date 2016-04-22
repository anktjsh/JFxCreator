/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Aniket
 */
public class FileUtils {

    public static boolean exists(File f) {
        return f.exists();
    }

    public static boolean createDirectories(File f) {
        return f.mkdirs();
    }

    public static boolean createDirectory(File f) {
        return f.mkdir();
    }

    public static boolean createFile(File f) {
        if (f.exists()) {
            return false;
        } else {
            write(f, new ArrayList<>());
            return true;
        }
    }

    public static void write(File f, List<String> al) {
        try (FileWriter fw = new FileWriter(f); PrintWriter out = new PrintWriter(fw)) {
            for (String s : al) {
                out.println(s);
            }
        } catch (IOException ex) {
        }
    }

    public static void copy(InputStream in, OutputStream out) {

    }

    public static void delete(File f) {

    }

    public static boolean isDirectory(File f) {
        return f.isDirectory();
    }

    public static boolean isHidden(File f) {
        return f.isHidden();
    }

    public static void move(File a, File b) {
        a.renameTo(b);
    }

    public static byte[] readAllBytes(File f) {
        FileInputStream fileInputStream;
        byte[] bFile = new byte[(int) size(f)];
        try {
            fileInputStream = new FileInputStream(f);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
        }
        return bFile;
    }

    public static List<String> readAllLines(File f) {
        ArrayList<String> al = new ArrayList<>();
        try {
            Scanner in = new Scanner(f);
            while (in.hasNextLine()) {
                al.add(in.nextLine());
            }
        } catch (IOException e) {
        }
        return al;
    }

    public static long size(File f) {
        return f.length();
    }
}
