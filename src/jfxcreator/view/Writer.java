/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxcreator.JFxCreator;
import static jfxcreator.JFxCreator.icon;
import jfxcreator.core.ProcessPool;
import jfxcreator.core.ProcessPool.ProcessItem;
import jfxcreator.core.Program;
import jfxcreator.core.Project;
import jfxcreator.core.ProjectTree;
import jfxcreator.core.ZipUtils;
import jfxcreator.view.FileWizard.FileDescription;

/**
 *
 * @author Aniket
 */
public class Writer extends BorderPane {
    
    public static final ObjectProperty<Font> fontSize = new SimpleObjectProperty(new Font(15));
    public static final BooleanProperty wrapText = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Project> currentProject = new SimpleObjectProperty(null);
    
    private final TabPane tabPane;
    private final MenuBar bar;
    private final Menu file, edit, launch, view, deploy, settings, help;
    private final MenuItem nFile, nProject, print, oFile, close, property, oProject, cProject, closeAll, save, saveAll, fullsc,
            undo, redo, cut, copy, paste, selectAll,
            build, clean, run,
            jar, zip, dNative,
            jPlatforms, pDirectory,
            about;
    private final CustomMenuItem modifyFontSize, wrapping;
    private final TreeView<String> tree;
    private final BorderPane top;
    private final ComboBox<String> fontSizes;
    private final BorderPane bottom;
    private final TabPane console;
    
    public Writer() {
        tabPane = new TabPane();
        setCenter(tabPane);
        setTop(top = new BorderPane(bar = new MenuBar()));
        top.setBottom(new EnvironmentToolBar(this));
        file = new Menu("File");
        edit = new Menu("Edit");
        launch = new Menu("Launch");
        view = new Menu("View");
        deploy = new Menu("Deploy");
        settings = new Menu("Settings");
        help = new Menu("Help");
        bar.getMenus().addAll(file, edit, launch, view, deploy, settings, help);
        file.getItems().addAll(nFile = new MenuItem("New File"),
                nProject = new MenuItem("New Project"),
                oFile = new MenuItem("Open File"),
                oProject = new MenuItem("Open Project"),
                cProject = new MenuItem("Close Project"),
                closeAll = new MenuItem("Close All Projects"),
                save = new MenuItem("Save"),
                saveAll = new MenuItem("Save All"),
                property = new MenuItem("Project Properties"),
                print = new MenuItem("Print"),
                fullsc = new MenuItem("Toggle FullScreen"),
                close = new MenuItem("Close "));
        edit.getItems().addAll(undo = new MenuItem("Undo"),
                redo = new MenuItem("Redo"),
                cut = new MenuItem("Cut"),
                copy = new MenuItem("Copy"),
                paste = new MenuItem("Paste"),
                selectAll = new MenuItem("Select All"));
        launch.getItems().addAll(build = new MenuItem("Build"),
                clean = new MenuItem("Clean and Build"),
                run = new MenuItem("Run"));
        CheckBox wrap;
        HBox hb;
        view.getItems().addAll(
                modifyFontSize = new CustomMenuItem(hb = new HBox(5, new Text("Modify Font Size"), fontSizes = new ComboBox<>())),
                wrapping = new CustomMenuItem(wrap = new CheckBox("Wrap Text"), false));
        modifyFontSize.setHideOnClick(false);
        wrap.setOnAction((e) -> {
            wrapText.set(wrap.isSelected());
        });
        hb.setAlignment(Pos.CENTER);
        fontSizes.setValue("" + fontSize.get().getSize());
        for (int x = 10; x <= 50; x++) {
            fontSizes.getItems().add("" + x);
        }
        fontSizes.setOnAction((e) -> {
            fontSize.set(new Font(Integer.parseInt(fontSizes.getValue())));
        });
        deploy.getItems().addAll(jar = new MenuItem("Deploy Jar"),
                zip = new MenuItem("Deploy Zip"),
                dNative = new MenuItem("Native Executable"));
        settings.getItems().addAll(jPlatforms = new MenuItem("Java Platforms"),
                pDirectory = new MenuItem("Workplace Directory"));
        help.getItems().add(about = new MenuItem("About"));
        
        nFile.setOnAction((e) -> {
            newFile();
        });
        nProject.setOnAction((e) -> {
            newProject();
        });
        oFile.setOnAction((e) -> {
            openFile();
        });
        oProject.setOnAction((e) -> {
            openProject();
        });
        cProject.setOnAction((e) -> {
            closeProject();
        });
        closeAll.setOnAction((e) -> {
            closeAllProjects();
        });
        save.setOnAction((e) -> {
            save();
        });
        saveAll.setOnAction((e) -> {
            saveAll();
        });
        property.setOnAction((e) -> {
            projectProperties();
        });
        print.setOnAction((e) -> {
            print();
        });
        fullsc.setOnAction((e) -> {
            Stage st = (Stage) getScene().getWindow();
            st.setFullScreen(!st.isFullScreen());
        });
        close.setOnAction((e) -> {
            getScene().getWindow().fireEvent(
                    new WindowEvent(
                            getScene().getWindow(),
                            WindowEvent.WINDOW_CLOSE_REQUEST
                    )
            );
        });
        undo.setOnAction((e) -> {
            undo();
        });
        redo.setOnAction((e) -> {
            redo();
        });
        cut.setOnAction((e) -> {
            cut();
        });
        copy.setOnAction((e) -> {
            copy();
        });
        paste.setOnAction((e) -> {
            paste();
        });
        selectAll.setOnAction((e) -> {
            selectAll();
        });
        build.setOnAction((e) -> {
            build();
        });
        clean.setOnAction((e) -> {
            clean();
        });
        run.setOnAction((e) -> {
            run();
        });
        jar.setOnAction((e) -> {
            clean.fire();
        });
        zip.setOnAction((e) -> {
            zip();
        });
        dNative.setOnAction((e) -> {
            executable();
        });
        jPlatforms.setOnAction((e) -> {
            javaPlatforms();
        });
        pDirectory.setOnAction((e) -> {
            directory();
        });
        about.setOnAction((e) -> {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("About");
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            al.setHeaderText("JFxCreator v1.0");
            al.setContentText("Created by Aniket Joshi");
            al.initOwner(getScene().getWindow());
            al.showAndWait();
        });
        
        tree = new TreeView();
        setLeft(tree);
        tree.setRoot(new TreeItem<>("Projects"));
        tree.getRoot().setExpanded(true);
        ProjectTree.getTree().addListener(new ProjectTree.ProjectTreeListener() {
            
            @Override
            public void projectAdded(Project added) {
                if (!projectContains(tree.getRoot().getChildren(), added)) {
                    ProjectTreeItem item;
                    tree.getRoot().getChildren().add(item = new ProjectTreeItem(added));
                    item.setExpanded(true);
                    added.addListener((new Project.ProjectListener() {
                        
                        @Override
                        public void fileAdded(Project pro, Program add) {
                            if (!scriptContains(item.getChildren(), add)) {
                                addScriptTreeItem(item, new ProgramTreeItem(add));
                            }
                        }
                        
                        @Override
                        public void fileRemoved(Project pro, Program scr) {
                            findScriptTreeItem(item, scr);
                            for (int x = tabPane.getTabs().size() - 1; x >= 0; x--) {
                                if (tabPane.getTabs().get(x) instanceof Editor) {
                                    Editor ed = (Editor) tabPane.getTabs().get(x);
                                    if (ed.getScript().equals(scr)) {
                                        ed.close();
                                    }
                                }
                            }
                        }
                    }));
                    added.getPrograms().stream().forEach((s) -> {
                        addScriptTreeItem(item, new ProgramTreeItem(s));
                    });
                }
            }
            
            @Override
            public void projectRemoved(Project pro) {
                
                for (TreeItem<String> tre : tree.getRoot().getChildren()) {
                    if (tre instanceof ProjectTreeItem) {
                        ProjectTreeItem pri = (ProjectTreeItem) tre;
                        if (pri.getProject().equals(pro)) {
                            if (pri.getParent() != null) {
                                pri.getParent().getChildren().remove(pri);
                                break;
                            }
                        }
                    }
                }
                for (int x = tabPane.getTabs().size() - 1; x >= 0; x--) {
                    Tab b = tabPane.getTabs().get(x);
                    if (b instanceof Editor) {
                        Editor ea = (Editor) b;
                        if (ea.getScript().getProject().equals(pro)) {
                            ea.close();
                        }
                    }
                }
            }
        });
        
        tree.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                if (e.getClickCount() == 2) {
                    TreeItem<String> sel = tree.getSelectionModel().getSelectedItem();
                    if (sel instanceof ProgramTreeItem) {
                        ProgramTreeItem sti = (ProgramTreeItem) sel;
                        boolean contains = true;
                        EnvironmentTab st = null;
                        for (Tab b : tabPane.getTabs()) {
                            if (b instanceof EnvironmentTab) {
                                EnvironmentTab ed = (EnvironmentTab) b;
                                if (ed.getScript().equals(sti.getScript())) {
                                    contains = false;
                                    st = ed;
                                }
                            }
                        }
                        if (contains) {
                            if (sti.getScript().getType() == Program.JAVA) {
                                Editor ed;
                                tabPane.getTabs().add(ed = new Editor(sti.getScript(), sti.getScript().getProject()));
                                tabPane.getSelectionModel().select(ed);
                            } else {
                                loadFile(sti.getScript().getFile().toFile(), sti.getScript(), sti.getScript().getProject());
                            }
                        } else {
                            tabPane.getSelectionModel().select(st);
                        }
                    }
                }
            }
        });
        tree.getSelectionModel().selectedItemProperty().addListener((ob, older, newer) -> {
            if (newer instanceof DirectoryTreeItem) {
                currentProject.set(((DirectoryTreeItem) newer).getProject());
            } else if (newer instanceof ProjectTreeItem) {
                currentProject.set(((ProjectTreeItem) newer).getProject());
            } else if (newer instanceof ProgramTreeItem) {
                currentProject.set(((ProgramTreeItem) newer).getScript().getProject());
            } else {
                currentProject.set(null);
            }
        });
        tree.setContextMenu(new ContextMenu());
        tree.setOnContextMenuRequested((e) -> {
            TreeItem<String> select = tree.getSelectionModel().getSelectedItem();
            if (select instanceof ProgramTreeItem) {
                ProgramTreeItem sti = (ProgramTreeItem) select;
                MenuItem open, delete, details;
                tree.getContextMenu().getItems().setAll(open = new MenuItem("Open File"),
                        delete = new MenuItem("Delete File"),
                        details = new MenuItem("File Details"));
                open.setOnAction((ae) -> {
                    boolean contains = true;
                    EnvironmentTab st = null;
                    for (Tab b : tabPane.getTabs()) {
                        if (b instanceof EnvironmentTab) {
                            EnvironmentTab ed = (EnvironmentTab) b;
                            if (ed.getScript().equals(sti.getScript())) {
                                contains = false;
                                st = ed;
                            }
                        }
                    }
                    if (contains) {
                        if (sti.getScript().getType() == Program.JAVA) {
                            Editor ed;
                            tabPane.getTabs().add(ed = new Editor(sti.getScript(), sti.getScript().getProject()));
                            tabPane.getSelectionModel().select(ed);
                        } else {
                            loadFile(sti.getScript().getFile().toFile(), sti.getScript(), sti.getScript().getProject());
                        }
                    } else {
                        tabPane.getSelectionModel().select(st);
                    }
                });
                delete.setOnAction((fe) -> {
                    Alert al = new Alert(Alert.AlertType.CONFIRMATION);
                    al.setTitle("Confirm");
                    al.setHeaderText("Are you sure you would like to delete this?");
                    al.initOwner(getScene().getWindow());
                    ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
                    Optional<ButtonType> show = al.showAndWait();
                    if (show.isPresent()) {
                        if (show.get() == ButtonType.OK) {
                            try {
                                Files.delete(sti.getScript().getFile());
                            } catch (IOException ex) {
                            }
                            sti.getScript().getProject().removeScript(sti.getScript());
                        }
                    }
                });
                details.setOnAction((ev) -> {
                    Details.getDetails((Stage) getScene().getWindow(), sti.getScript().getFile()).showAndWait();
                });
            } else if (select instanceof DirectoryTreeItem) {
                tree.getContextMenu().getItems().clear();
            } else if (select instanceof ProjectTreeItem) {
                MenuItem clsepse, delete, prop, details;
                tree.getContextMenu().getItems().setAll(clsepse = new MenuItem("Close Project"),
                        delete = new MenuItem("Delete Project"),
                        details = new MenuItem("Project Details"),
                        prop = new MenuItem("Properties")
                );
                clsepse.setOnAction((c) -> {
                    closeProject(((ProjectTreeItem) select).getProject());
                });
                delete.setOnAction((fe) -> {
                    Alert al = new Alert(Alert.AlertType.CONFIRMATION);
                    al.setTitle("Confirm");
                    al.setHeaderText("Are you sure you would like to delete this?");
                    ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
                    al.initOwner(getScene().getWindow());
                    Optional<ButtonType> show = al.showAndWait();
                    if (show.isPresent()) {
                        if (show.get() == ButtonType.OK) {
                            clsepse.fire();
                            ((ProjectTreeItem) select).getProject().delete();
                        }
                    }
                });
                prop.setOnAction((efd) -> {
                    //property(((ProjectTreeItem) select).getProject());
                });
                details.setOnAction((ejgf) -> {
                    Details.getDetails((Stage) getScene().getWindow(), ((ProjectTreeItem) select).getProject().getRootDirectory()).showAndWait();
                });
            } else {
                tree.getContextMenu().getItems().clear();
            }
        });
        resize(bar, "-fx-font-size:15");
        fontSize.addListener((ob, older, newer) -> {
            resize(bar, "-fx-font-size:" + newer.getSize());
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener((ob, older, newer) -> {
            if (newer instanceof Editor) {
                currentProject.set(((Editor) newer).getScript().getProject());
            } else if (newer instanceof Viewer) {
                currentProject.set(((Viewer) newer).getScript().getProject());
            } else {
                currentProject.set(null);
            }
        });
        openPreviousProjects();
        addDragAndDrop();
        setOnKeyPressed((e) -> {
            evaluate(e);
        });
        bottom = new BorderPane();
        console = new TabPane();
        console.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) -> {
            c.next();
            if (c.getList().isEmpty()) {
                bottom.setCenter(null);
                bottom.setCenter(console);
            }
        });
        setBottom(bottom);
        bottom.setCenter(console);
        bottom.setPadding(new Insets(5, 10, 5, 10));
        
    }
    
    private void findScriptTreeItem(ProjectTreeItem pro, Program scr) {
        for (int x = pro.getChildren().size() - 1; x >= 0; x--) {
            TreeItem<String> al = pro.getChildren().get(x);
            if (al instanceof ProgramTreeItem) {
                ProgramTreeItem sti = (ProgramTreeItem) al;
                if (sti.getScript().equals(scr)) {
                    pro.getChildren().remove(sti);
                }
            } else if (al instanceof DirectoryTreeItem) {
                DirectoryTreeItem dir = (DirectoryTreeItem) al;
                findScriptTreeItem(dir, scr);
            }
        }
    }
    
    private boolean projectContains(ObservableList<TreeItem<String>> tre, Project pr) {
        return tre.stream().filter((tr) -> (tr instanceof ProjectTreeItem)).map((tr) -> (ProjectTreeItem) tr).anyMatch((pra) -> (pra.getProject().equals(pr)));
    }
    
    private boolean scriptContains(ObservableList<TreeItem<String>> pti, Program pro) {
        for (TreeItem<String> tr : pti) {
            if (tr instanceof ProgramTreeItem) {
                ProgramTreeItem pra = (ProgramTreeItem) tr;
                if (pra.getScript().equals(pro)) {
                    return true;
                }
            } else if (tr instanceof DirectoryTreeItem) {
                DirectoryTreeItem dti = (DirectoryTreeItem) tr;
                return scriptContains(dti.getChildren(), pro);
            }
        }
        return false;
    }
    
    private void addScriptTreeItem(ProjectTreeItem pti, ProgramTreeItem sti) {
        String one, two;
        one = pti.getProject().getRootDirectory().toAbsolutePath().toString() + File.separator + "src" + File.separator;
        two = sti.getScript().getFile().toAbsolutePath().toString();
        String left = two.substring(one.length());
        if (left.contains(File.separator)) {
            DirectoryTreeItem last = null;
            while (left.contains(File.separator)) {
                Path a = Paths.get(one + File.separator + left.substring(0, left.indexOf(File.separator) + 1));
                DirectoryTreeItem dti = getDirectoryItem(pti, a.getFileName().toString());
                if (dti == null) {
                    dti = new DirectoryTreeItem(pti.getProject(), a);
                    pti.getChildren().add(dti);
                }
                left = left.substring(left.indexOf(File.separator) + 1);                
                pti = dti;
                last = dti;
            }
            if (last != null) {
                last.getChildren().add(sti);
            }
        } else {
            pti.getChildren().add(sti);
        }
    }
    
    private DirectoryTreeItem getDirectoryItem(ProjectTreeItem pro, String name) {
        for (TreeItem<String> tri : pro.getChildren()) {
            if (tri instanceof DirectoryTreeItem) {
                if (tri.getValue().equals(name)) {
                    return (DirectoryTreeItem) tri;
                } else {
                    return getDirectoryItem((DirectoryTreeItem) tri, name);
                }
            }
        }
        return null;
    }
    
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    private void addDragAndDrop() {
        if (getScene() == null) {
            sceneProperty().addListener((ob, older, newer) -> {
                if (newer != null) {
                    newer.setOnDragOver((event) -> {
                        Dragboard db = event.getDragboard();
                        if (db.hasFiles()) {
                            event.acceptTransferModes(TransferMode.COPY);
                        } else {
                            event.consume();
                        }
                    });
                    newer.setOnDragDropped((event) -> {
                        Dragboard db = event.getDragboard();
                        boolean success = false;
                        if (db.hasFiles()) {
                            success = true;
                            loadFiles(db.getFiles());
                        }
                        event.setDropCompleted(success);
                        event.consume();
                    });
                }
            });
        } else {
            getScene().setOnDragOver((event) -> {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            });
            getScene().setOnDragDropped((event) -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    loadFiles(db.getFiles());
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }
    }
    
    private void loadFiles(List<File> fil) {
        fil.stream().forEach((f) -> {
            loadFile(f, null);
        });
    }
    
    private void loadFile(File f, Program prog, Project parent) {
        if (prog == null) {
            if (f.isDirectory()) {
                Project p = Project.unserialize(f.toPath());
                if (p != null) {
                    ProjectTree.getTree().addProject(p);
                }
            } else if (f.isFile()) {
                String type = "";
                try {
                    type = Files.probeContentType(f.toPath());
                } catch (IOException ef) {
                }
                if (type != null) {
                    if (type.contains("text")) {
                        Editor ed;
                        Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                        tabPane.getTabs().add(ed = new Editor(pro, parent));
                        tabPane.getSelectionModel().select(ed);
                    } else if (type.contains("image")) {
                        Viewer vi;
                        Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                        tabPane.getTabs().add(vi = new Viewer(pro, parent));
                        tabPane.getSelectionModel().select(vi);
                    } else {
                        if (alert()) {
                            Editor vi;
                            Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                            tabPane.getTabs().add(vi = new Editor(pro, parent));
                            tabPane.getSelectionModel().select(vi);
                        }
                    }
                } else {
                    if (alert()) {
                        Editor vi;
                        Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                        tabPane.getTabs().add(vi = new Editor(pro, parent));
                        tabPane.getSelectionModel().select(vi);
                    }
                }
            }
        } else {
            String type = "";
            try {
                type = Files.probeContentType(prog.getFile());
            } catch (IOException ef) {
            }
            if (type != null) {
                if (type.contains("text")) {
                    Editor ed;
                    tabPane.getTabs().add(ed = new Editor(prog, prog.getProject()));
                    tabPane.getSelectionModel().select(ed);
                } else if (type.contains("image")) {
                    Viewer vi;
                    tabPane.getTabs().add(vi = new Viewer(prog, prog.getProject()));
                    tabPane.getSelectionModel().select(vi);
                } else {
                    if (alert()) {
                        Editor vi;
                        tabPane.getTabs().add(vi = new Editor(prog, prog.getProject()));
                        tabPane.getSelectionModel().select(vi);
                    }
                }
            } else {
                if (alert()) {
                    Editor vi;
                    tabPane.getTabs().add(vi = new Editor(prog, prog.getProject()));
                    tabPane.getSelectionModel().select(vi);
                }
            }
        }
    }
    
    private void loadFile(File f, Project parent) {
        loadFile(f, null, parent);
    }
    
    public final void openPreviousProjects() {
        Path f = Paths.get(".cache" + File.separator + "previous03.txt");
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(f));
        } catch (IOException ex) {
        }
        al.stream().map((s) -> Project.unserialize(s)).filter((p) -> (p != null)).forEach((p) -> {
            ProjectTree.getTree().addProject(p);
        });
        openPreviousTabs();
    }
    
    private void openPreviousTabs() {
        Path f = Paths.get(".cache" + File.separator + "open03.txt");
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(f));
        } catch (IOException ex) {
        }
        ArrayList<Program> asc = new ArrayList<>();
        ProjectTree.getTree().getProjects().stream().forEach((p) -> {
            p.getPrograms().stream().forEach((sc) -> {
                for (int x = al.size() - 1; x >= 0; x--) {
                    if (sc.getFile().toAbsolutePath().toString().equals(al.get(x))) {
                        asc.add(sc);
                        al.remove(x);
                    }
                }
            });
        });
        if (!al.isEmpty()) {
            ArrayList<File> af = new ArrayList<>();
            al.stream().forEach((s) -> {
                af.add(new File(s));
            });
            loadFiles(af);
        }
        asc.stream().forEach((sti) -> {
            if (sti.getType() == Program.JAVA) {
                Editor ed;
                tabPane.getTabs().add(ed = new Editor(sti, sti.getProject()));
                tabPane.getSelectionModel().select(ed);
            } else {
                loadFile(sti.getFile().toFile(), sti, sti.getProject());
                
            }
        });
    }
    
    private boolean alert() {
        if (getScene() == null) {
            return true;
        }
        if (getScene().getWindow() == null) {
            return true;
        }
        Alert al = new Alert(AlertType.ERROR);
        al.setTitle("File Type");
        ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
        al.setHeaderText("ScriptFx is unable to read this type of File");
        al.setContentText("Would you still like to open this File?");
        al.initOwner(getScene().getWindow());
        Optional<ButtonType> show = al.showAndWait();
        if (show.isPresent()) {
            if (show.get() == ButtonType.OK) {
                return true;
            }
        }
        return false;
    }
    
    private void resize(MenuBar bar, String style) {
        bar.getMenus().stream().forEach((m) -> {
            m.setStyle(style);
        });
        tree.setStyle(style);
    }
    
    public boolean processCheck() {
        int size = ProcessPool.getPool().size();
        if (size == 0) {
            return true;
        } else {
            Alert al = new Alert(Alert.AlertType.CONFIRMATION);
            al.setTitle("Running Processes");
            al.initOwner(getScene().getWindow());
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            al.setHeaderText(ProcessPool.getPool().getAccumulatedText());
            al.setContentText("These processes are currently running!\n"
                    + "Would you like to cancel all of the processes?");
            Optional<ButtonType> show = al.showAndWait();
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    ProcessPool.getPool().cancel();
                    return true;
                }
            }
            return false;
        }
    }
    
    public final void clean() {
        if (getCurrentProject() != null) {
            ProcessItem cleanAndBuild = getCurrentProject().cleanAndBuild();
            addConsoleWindow(cleanAndBuild);
        }
    }
    
    public final void undo() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().undo();
        }
    }
    
    public final void redo() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().redo();
        }
    }
    
    public final void cut() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().cut();
        }
    }
    
    public final void copy() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().copy();
        }
    }
    
    public final void paste() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().paste();
        }
    }
    
    public final void selectAll() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().selectAll();
        }
    }
    
    public final void saveAll() {
        for (Tab b : tabPane.getTabs()) {
            if (b instanceof Editor) {
                Editor ed = (Editor) b;
                ed.save();
            }
        }
    }
    
    private void save() {
        if (getSelectedEditor() != null) {
            getSelectedEditor().save();
        }
    }
    
    private void print() {
        if (getSelectedTab() != null) {
            ObservableSet<Printer> allP = Printer.getAllPrinters();
            ObservableList<Printer> printers = FXCollections.observableArrayList();
            ObservableList<String> names = FXCollections.observableArrayList();
            allP.stream().map((p) -> {
                printers.add(p);
                return p;
            }).forEach((p) -> {
                names.add(p.getName());
            });
            ChoiceDialog<String> dialog = new ChoiceDialog<>(Printer.getDefaultPrinter().getName(), names);
            dialog.setTitle("Printer Options");
            dialog.setHeaderText("Choose your Printer");
            dialog.initOwner(getScene().getWindow());
            ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                Printer pri = null;
                for (Printer p : printers) {
                    if (p.getName().equals(result.get())) {
                        pri = p;
                    }
                }
                if (pri != null) {
                    PrinterJob job = PrinterJob.createPrinterJob(pri);
                    if (job != null) {
                        boolean suc = job.printPage(getSelectedTab().getContent());
                        if (suc) {
                            job.endJob();
                            Alert al = new Alert(Alert.AlertType.INFORMATION);
                            al.setTitle("Printer");
                            al.initOwner(getScene().getWindow());
                            al.setHeaderText("Print Complete");
                            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
                            al.showAndWait();
                        }
                    }
                }
            }
        }
    }
    
    private void openFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        List<File> multi = fc.showOpenMultipleDialog(getScene().getWindow());
        loadFiles(multi);
    }
    
    public final void openProject() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(Dependencies.workplace_location));
        File show = dc.showDialog(getScene().getWindow());
        if (show != null) {
            Project pro = Project.loadProject(show.toPath(), false);
            if (pro != null) {
                ProjectTree.getTree().addProject(pro);
            }
        }
    }
    
    public final void newFile() {
        if (getCurrentProject() != null) {
            Optional<FileDescription> show = new FileWizard(getScene().getWindow(), getCurrentProject()).showAndWait();
            if (show.isPresent()) {
                Program sc;
                if (show.get().getDescription().contains("Other")) {
                    sc = new Program(Program.RESOURCE,
                            Paths.get(getCurrentProject().getSource().toAbsolutePath().toString() + File.separatorChar + Program.getFilePath(show.get().getName())),
                            FileWizard.getTemplateCode(show.get().getDescription(),
                                    show.get().getName()),
                            getCurrentProject());
                } else {
                    sc = new Program(Program.JAVA,
                            show.get().getName(),
                            Paths.get(getCurrentProject().getSource().toAbsolutePath().toString() + File.separator + Program.getFilePath(show.get().getName()) + ".java"),
                            FileWizard.getTemplateCode(show.get().getDescription(),
                                    show.get().getName()),
                            getCurrentProject());
                }
                getCurrentProject().addScript(sc);
                loadFile(sc.getFile().toFile(), sc, sc.getProject());
            }
        } else {
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("New File");
            al.initOwner(getScene().getWindow());
            al.setContentText("Select a Project to Create a New File");
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            al.showAndWait();
        }
    }
    
    public final void newProject() {
        Project pro = ProjectWizard.createProject(getScene().getWindow());
        if (pro != null) {
            ArrayList<Program> scripts = pro.getPrograms();
            scripts.stream().map((s) -> {
                Editor ed;
                tabPane.getTabs().add(ed = new Editor(s, pro));
                return ed;
            }).forEach((ed) -> {
                tabPane.getSelectionModel().select(ed);
            });
            ProjectTree.getTree().addProject(pro);
        }
    }
    
    public final void run() {
        if (getCurrentProject() != null) {
            ProcessItem run1 = getCurrentProject().run();
            addConsoleWindow(run1);
        }
    }
    
    public final void build() {
        if (getCurrentProject() != null) {
            ProcessItem builder = getCurrentProject().build();
            addConsoleWindow(builder);
        }
    }
    
    public final void closeProject() {
        if (getCurrentProject() != null) {
            closeProject(getCurrentProject());
        }
    }
    
    private void closeProject(Project pro) {
        ProjectTree.getTree().removeProject(pro);
    }
    
    public final void closeAllProjects() {
        for (Project p : ProjectTree.getTree().getProjects()) {
            closeProject(p);
        }
    }
    
    public final void executable() {
        //
    }
    
    public final void zip() {
        String out = getCurrentProject().getDist().toAbsolutePath().toString()
                + File.separator + getCurrentProject().getProjectName() + ".zip";
        ZipUtils appZip = new ZipUtils();
        String source = getCurrentProject().getSource().toAbsolutePath().toString();
        appZip.generateFileList(new File(source), source);
        appZip.zipIt(source, out);
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.initOwner(getScene().getWindow());
        al.setTitle("Zip Deploy");
        al.setHeaderText("Zip File has been created and placed in Directory : " + getCurrentProject().getDist().toAbsolutePath().toString());
        ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(icon);
        al.showAndWait();
    }
    
    public final void projectProperties() {
        //
    }
    
    public final void javaPlatforms() {
        Dependencies.platform(getScene().getWindow());
    }
    
    public final void directory() {
        Dependencies.workplace(getScene().getWindow());
    }
    
    public boolean canSave() {
        return tabPane.getTabs().stream().filter((b) -> (b instanceof Editor)).map((b) -> (Editor) b).anyMatch((ed) -> (ed.canSave()));
    }
    
    public void saveOpenProjectsInformation() {
        saveOpenTabsInformation();
        Path p = Paths.get(".cache");
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException ex) {
            }
        }
        Path f = Paths.get(".cache" + File.separator + "previous03.txt");
        ArrayList<String> al = new ArrayList<>();
        ObservableList<TreeItem<String>> children = tree.getRoot().getChildren();
        children.stream().filter((tre) -> (tre instanceof ProjectTreeItem)).map((tre) -> (ProjectTreeItem) tre).forEach((pti) -> {
            al.add(pti.getProject().serialize());
        });
        try {
            Files.write(f, al);
        } catch (IOException ex) {
        }
    }
    
    private void saveOpenTabsInformation() {
        Path p = Paths.get(".cache");
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException ex) {
            }
        }
        Path f = Paths.get(".cache" + File.separator + "open03.txt");
        ArrayList<String> al = new ArrayList<>();
        for (Tab b : tabPane.getTabs()) {
            if (b instanceof EnvironmentTab) {
                EnvironmentTab sb = (EnvironmentTab) b;
                al.add(sb.getScript().getFile().toAbsolutePath().toString());
            }
        }
        try {
            Files.write(f, al);
        } catch (IOException e) {
        }
    }
    
    private Editor getSelectedEditor() {
        if (getSelectedTab() == null) {
            return null;
        } else if (getSelectedTab() instanceof Editor) {
            return (Editor) getSelectedTab();
        }
        return null;
    }
    
    private EnvironmentTab getSelectedTab() {
        if (tabPane.getSelectionModel().getSelectedItem() != null) {
            if (tabPane.getSelectionModel().getSelectedItem() instanceof EnvironmentTab) {
                return ((EnvironmentTab) tabPane.getSelectionModel().getSelectedItem());
            }
        }
        return null;
    }
    
    private void addConsoleWindow(ProcessItem c) {
        ConsoleWindow con;
        console.getTabs().add(con = new ConsoleWindow(c));
        console.getSelectionModel().select(con);
    }
    
    public final void evaluate(KeyEvent ke) {
        
    }
    
}
