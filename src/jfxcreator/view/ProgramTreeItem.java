/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.TreeItem;
import jfxcreator.core.Program;

/**
 *
 * @author Aniket
 */
public class ProgramTreeItem extends TreeItem<String> {

    private final Program script;

    public ProgramTreeItem(Program pro) {
        setValue(pro.getFile().getFileName().toString());
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
