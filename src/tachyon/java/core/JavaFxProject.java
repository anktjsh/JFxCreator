/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.java.core;

import tachyon.java.core.JavaProgram;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javafx.collections.FXCollections;
import tachyon.framework.core.Program;
import tachyon.java.manager.JavaFxManager;
import tachyon.framework.manager.TaskManager;
import tachyon.view.FileWizard;

/**
 *
 * @author Aniket
 */
public class JavaFxProject extends JavaProject {

    public JavaFxProject(Path rot, boolean isNew, String mcn) {
        super(rot, isNew, mcn);
    }

    @Override
    protected TaskManager constructManager() {
        return new JavaFxManager(this);
    }

    @Override
    protected void initializeProject() {
        JavaProgram pro = new JavaProgram(Paths.get(getSource().toAbsolutePath() + Program.getFilePath(getMainClassName()) + ".java"),
                FileWizard.getTemplateCode("JavaFx Main Class", getMainClassName()),
                this, getMainClassName());
        addScript(pro);
    }

    @Override
    protected void saveConfig() {
        try {
            Files.write(getConfig(),
                    FXCollections.observableArrayList(getMainClassName(),
                            "Libs : " + getAllLibs().toString(),
                            getCompileTimeArguments().toString(),
                            getRuntimeArguments().toString(),
                            getIconFilePath() == null ? "" : getIconFilePath(),
                            Arrays.toString(getClass().getName().getBytes())
                    ));
        } catch (IOException e) {
        }
    }

}
