/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.manager;

import tachyon.core.DebuggerController;
import tachyon.core.Program;
import tachyon.core.Project;
import tachyon.process.ProcessItem;
import tachyon.view.Dependencies;

/**
 *
 * @author Aniket
 */
public abstract class ParentManager {

    private final Project project;

    public ParentManager(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }
    
    protected static String getJavaHomeLocation() {
        return Dependencies.localVersionProperty.get();
    }

    public abstract void compile(ProcessItem pro);

    public abstract void build(ProcessItem pro);

    public abstract void run(ProcessItem pro);

    public abstract void nativeExecutable(ProcessItem pro);

    public abstract void runIndividualFile(ProcessItem pro, Program java);

    public abstract void fatBuild(ProcessItem pro);
    
    public abstract void debugProject(ProcessItem pro, DebuggerController con);
}
