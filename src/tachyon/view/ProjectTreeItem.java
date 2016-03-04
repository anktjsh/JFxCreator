/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.util.Arrays;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class ProjectTreeItem extends TreeItem<String> {

    public static final Image proj = new Image(LibraryTreeItem.class.getResourceAsStream("tree/project.PNG"), 25, 25, true, true);
    private static final Image folder = new Image(LibraryTreeItem.class.getResourceAsStream("tree/folder.PNG"), 25, 25, true, true);
    private final Project project;
    private TreeItem<String> src, libs;
    private PlatformTreeItem platform;

    public ProjectTreeItem(Project pro, boolean b) {
        super(pro.getRootDirectory().getFileName().toString());
        project = pro;
        setGraphic(new ImageView(proj));
        if (b) {
            src = new TreeItem<>("Source Packages");
            src.setGraphic(new ImageView(folder));
            libs = new TreeItem<>("Libraries");
            libs.setGraphic(new ImageView(folder));
            platform = new PlatformTreeItem(pro);
            getChildren().addAll(Arrays.asList(src, libs, platform));
        }
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
