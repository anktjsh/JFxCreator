/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.TreeItem;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class ProjectTreeItem extends TreeItem<String> {

    private final Project project;

    public ProjectTreeItem(Project pro) {
        super(pro.getRootDirectory().getFileName().toString());
        project = pro;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProjectTreeItem) {
            ProjectTreeItem pt = (ProjectTreeItem) obj;
            return pt.getProject().equals(getProject());
        }
        return false;
    }

}
