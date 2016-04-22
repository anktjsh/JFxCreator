/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import tachyon.core.JavaProgram;
import tachyon.core.JavaProgram.JavaProgramListener;
import tachyon.core.Program;

/**
 *
 * @author Aniket
 */
public class ProgramTreeItem extends TreeItem<String> {

    private static final Image file = new Image(ProgramTreeItem.class.getResourceAsStream("tree/file.png"), 25, 25, true, true);
    private static final Image java_file = new Image(ProgramTreeItem.class.getResourceAsStream("tree/java.png"), 25, 25, true, true);
    private static final Image image_file = new Image(ProgramTreeItem.class.getResourceAsStream("tree/image.png"), 25, 25, true, true);
    private static final Image text_file = new Image(ProgramTreeItem.class.getResourceAsStream("tree/text.png"), 25, 25, true, true);
    private static final Image fxml_file = new Image(ProgramTreeItem.class.getResourceAsStream("tree/fxml.png"), 25, 25, true, true);
    private final Program script;
    private final HBox graphic;

    public ProgramTreeItem(Program pro) {
        setValue(pro.getFileName());
        script = pro;
        graphic = new HBox();
        graphic.getChildren().add(new ImageView(getIconImage()));
        setGraphic(graphic);
        if (script instanceof JavaProgram) {
            ((JavaProgram) script).addProgramListener(new JavaProgramListener() {
                @Override
                public void hasErrors(JavaProgram pro, TreeMap<Long, String> errors) {
                    Platform.runLater(() -> {
                        if (errors.isEmpty()) {
                            if (graphic.getChildren().get(0) instanceof Text) {
                                graphic.getChildren().remove(0);
                            }
                        } else if (!(graphic.getChildren().get(0) instanceof Text)) {
                            Text t;
                            graphic.getChildren().add(0, t = new Text("X"));
                            t.setFill(Color.RED);
                        }
                    });
                }

                @Override
                public void hasBreakPoints(JavaProgram pro, List<Long> points) {

                }
            });
        }
    }

    private Image getIconImage() {
        if (getScript() instanceof JavaProgram) {
            return java_file;
        }
        String type = null;
        try {
            type = Files.probeContentType(getScript().getFile());
        } catch (IOException e) {
        }
        if (type != null) {
            if (type.contains("image")) {
                return image_file;
            }
            if (type.contains("text")) {
                return text_file;
            }
            if (type.contains("fxml")) {
                return fxml_file;
            }
        }
        return file;
    }

    public Program getScript() {
        return script;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProgramTreeItem) {
            ProgramTreeItem pt = (ProgramTreeItem) obj;
            return pt.getScript().equals(getScript());
        }
        return false;
    }

}
