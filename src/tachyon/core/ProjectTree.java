/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class ProjectTree {

    private static ProjectTree tree;

    public static ProjectTree getTree() {
        if (tree == null) {
            tree = new ProjectTree();
        }
        return tree;
    }

    private final ObservableList<Project> projects;
    private final ObservableList<ProjectTreeListener> listeners;

    public ProjectTree() {
        projects = FXCollections.observableArrayList();
        listeners = FXCollections.observableArrayList();
    }

    public void addProject(Project pro) {
        projects.add(pro);
        listeners.stream().forEach((lt) -> {
            lt.projectAdded(pro);
        });
    }

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public void removeProject(Project pro) {
        projects.remove(pro);
        pro.close();
        listeners.stream().forEach((lt) -> {
            lt.projectRemoved(pro);
        });
    }

    public void close() {
        projects.stream().forEach((p) -> {
            p.close();
        });
    }

    public void addListener(ProjectTreeListener al) {
        listeners.add(al);
    }

    public interface ProjectTreeListener {

        public void projectAdded(Project pro);

        public void projectRemoved(Project pro);
    }
}
