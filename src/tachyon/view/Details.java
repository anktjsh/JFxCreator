/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class Details {

    private final Path path;
    private Long size;

    private Details(Path pa) {
        path = pa;
    }

    public Path getPath() {
        return path;
    }

    public Long size() {
        if (size == null) {
            try {
                size = countSize(path.toFile());
            } catch (IOException ex) {
                if (size == null) {
                    size = 0L;
                }
            }
        }
        return size;
    }

    private static VBox getDetails(Path p) {
        Details d = new Details(p);
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.getChildren().addAll(new Text("File Name : " + d.getPath().getFileName().toString()),
                new Text("Absolute Path : " + d.getPath().toAbsolutePath().toString()),
                new Text("File is Directory : " + Files.isDirectory(d.getPath())),
                new Text("File Size : " + d.size() + " bytes"));
        return box;
    }

    public static Stage getDetails(Stage w, Path p) {
        Stage stage = new Stage();
        stage.initOwner(w);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Details");
        stage.getIcons().addAll(w.getIcons());
        stage.setScene(new Scene(getDetails(p)));
        return stage;
    }

    private static long countSize(File f) throws IOException {
        long ret = 0;
        if (f.isDirectory()) {
            for (File fa : f.listFiles()) {
                ret += countSize(fa);
            }
        } else {
            ret += f.length();
        }
        return ret;
    }
}
