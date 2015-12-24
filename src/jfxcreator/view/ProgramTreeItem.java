/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jfxcreator.core.Program;

/**
 *
 * @author Aniket
 */
public class ProgramTreeItem extends TreeItem<String> {

    private static final Image file = new Image(ProgramTreeItem.class.getResourceAsStream("tree/file.PNG"), 25, 25, true, true);
    private final Program script;

    public ProgramTreeItem(Program pro) {
        setValue(pro.getFile().getFileName().toString());
        setGraphic(new ImageView(file));
        script = pro;
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
