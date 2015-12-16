/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import jfxcreator.core.Program;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class EnvironmentTab extends Tab {

    private final BorderPane content;
    private final Project project;
    private final Program script;

    public EnvironmentTab(Program sc, Project pro) {
        super(sc.getFile().getFileName().toString());
        project = pro;
        content = new BorderPane();
        setContent(content);
        script = sc;
    }

    public BorderPane getCenter() {
        return content;
    }

    public Program getScript() {
        return script;
    }

    public Project getProject() {
        return project;
    }
}
