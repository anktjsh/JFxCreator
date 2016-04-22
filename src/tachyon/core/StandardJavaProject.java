/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javafx.collections.FXCollections;
import tachyon.manager.JavaManager;
import tachyon.manager.ParentManager;
import tachyon.view.FileWizard;

/**
 *
 * @author Aniket
 */
public class StandardJavaProject extends JavaProject{

    public StandardJavaProject(Path rot, boolean isNew, String mcn) {
        super(rot, isNew, mcn);
    }

    @Override
    protected ParentManager constructManager() {
        return new JavaManager(this);
    }

    @Override
    protected void initializeProject() {
        JavaProgram pro = new JavaProgram(Paths.get(getSource().toAbsolutePath() + Program.getFilePath(getMainClassName()) + ".java"),
                    FileWizard.getTemplateCode("Java Main Class", getMainClassName()),
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
