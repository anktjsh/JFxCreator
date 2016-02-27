/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import tachyon.Tachyon;
import static tachyon.Tachyon.icon;
import tachyon.contact.EmailPicker;
import tachyon.core.Console;
import tachyon.core.DebuggerController;
import tachyon.core.Examples;
import tachyon.core.JavaLibrary;
import tachyon.core.ProcessItem;
import tachyon.core.ProcessPool;
import tachyon.core.Program;
import tachyon.core.Project;
import tachyon.core.ProjectTree;
import tachyon.core.TaskManager;
import tachyon.core.Template;
import tachyon.core.ZipUtils;
import tachyon.memory.Monitor;
import tachyon.view.FileWizard.FileDescription;
import org.fxmisc.richtext.Paragraph;

/**
 *
 * @author Aniket
 */
public class Writer extends BorderPane {

    public static final ObjectProperty<Font> fontSize = new SimpleObjectProperty<>(new Font(15));
    public static final BooleanProperty wrapText = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>(null);

    private final TabPane tabPane;
    private final MenuBar bar;
    private final Menu file, edit, launch, debug, deploy, settings, help, source, memory;
    private final MenuItem nFile, nProject, print, oFile, close, property, oProject, cProject, closeAll, save, saveAll, fullsc,
            undo, redo, cut, copy, paste, selectAll,
            build, clean, run, runF, launchJar,
            debugP,
            jar, zip, dNative,
            jPlatforms, pDirectory, view,
            monitor,
            examples,
            request, report, information;
    private final Menu templates;
    private final MenuItem newTemplate, selectTemplate;
    private final ProjectsView projects;
    private final BorderPane top;
    private final BorderPane bottom;
    private final TabPane console;

    public Writer() {
        tabPane = new TabPane();
        setCenter(tabPane);
        setTop(top = new BorderPane(bar = new MenuBar()));
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("mac")) {
            bar.setUseSystemMenuBar(true);
        }
        top.setBottom(new EnvironmentToolBar(this));
        file = new Menu("File");
        edit = new Menu("Edit");
        launch = new Menu("Launch");
        debug = new Menu("Debug");
        deploy = new Menu("Deploy");
        settings = new Menu("Settings");
        source = new Menu("Source");
        memory = new Menu("Memory");
        help = new Menu("Help");
        bar.getMenus().addAll(file, edit, launch, debug, deploy, settings, source, memory, help);
        file.getItems().addAll(nFile = new MenuItem("New File\t\t\t\tCtrl+N"),
                nProject = new MenuItem("New Project\t\tCtrl+Shift+N"),
                oFile = new MenuItem("Open File\t\t\t\tCtrl+O"),
                oProject = new MenuItem("Open Project\t\tCtrl+Shift+O"),
                cProject = new MenuItem("Close Project\t\tCtrl+Shift+C"),
                closeAll = new MenuItem("Close All Projects"),
                save = new MenuItem("Save\t\t\t\t\tCtrl+S"),
                saveAll = new MenuItem("Save All\t\t\tCtrl+Shift+S"),
                property = new MenuItem("Project Properties"),
                print = new MenuItem("Print"),
                fullsc = new MenuItem("Toggle FullScreen"),
                close = new MenuItem("Exit"));
        edit.getItems().addAll(undo = new MenuItem("Undo\t\tCtrl+Z"),
                redo = new MenuItem("Redo\t\tCtrl+Y"),
                cut = new MenuItem("Cut\t\t\tCtrl+X"),
                copy = new MenuItem("Copy\t\tCtrl+C"),
                paste = new MenuItem("Paste\t\tCtrl+V"),
                selectAll = new MenuItem("Select All\t\tCtrl+A"));
        launch.getItems().addAll(build = new MenuItem("Build"),
                clean = new MenuItem("Clean and Build"),
                run = new MenuItem("Run"),
                runF = new MenuItem("Run File"),
                launchJar = new MenuItem("Launch Jar File"));
        debug.getItems().addAll(debugP = new MenuItem("Debug Project"));
        deploy.getItems().addAll(jar = new MenuItem("Deploy Jar"),
                zip = new MenuItem("Deploy Zip"),
                dNative = new MenuItem("Native Executable"));
        settings.getItems().addAll(jPlatforms = new MenuItem("Java Platforms"),
                pDirectory = new MenuItem("Workplace Directory"),
                view = new MenuItem("View"));
        source.getItems().addAll(templates = new Menu("Templates"),
                examples = new MenuItem("Examples"));
        memory.getItems().addAll(monitor = new MenuItem("Monitor"));
        templates.getItems().addAll(newTemplate = new MenuItem("New Template"),
                selectTemplate = new MenuItem("Select Template"));
        help.getItems().addAll(
                information = new MenuItem("About"),
                report = new MenuItem("Report a Bug"),
                request = new MenuItem("Request a Feature"));
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
        monitor.setOnAction((e) -> {
            Monitor.show();
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
        runF.setOnAction((e) -> {
            runFile();
        });
        launchJar.setOnAction((e) -> {
            launchJar();
        });
        debugP.setOnAction((e) -> {
            debug();
        });
        jar.setOnAction((e) -> {
            fatJar();
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
        view.setOnAction((E) -> {
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setResizable(false);
            st.getIcons().add(Tachyon.icon);
            st.initOwner(getScene().getWindow());
            st.setWidth(450);
            st.setHeight(300);
            st.setTitle("View");
            VBox box;
            st.setScene(new Scene(box = new VBox(10)));
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(5, 10, 5, 10));
            CheckBox wrap;
            HBox hb;
            ComboBox<String> fontSizes;
            box.getChildren().add(hb = new HBox(5, new Text("Modify Font Size"), fontSizes = new ComboBox<>()));
            box.getChildren().add(wrap = new CheckBox("Wrap Text"));
            Button hide;
            box.getChildren().add(hide = new Button("Close"));
            wrap.setSelected(wrapText.get());
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
            hide.setOnAction((e) -> {
                st.close();
            });
            st.showAndWait();
        });
        newTemplate.setOnAction((e) -> {
            if (getSelectedEditor() != null) {
                TextInputDialog tid = new TextInputDialog();
                tid.setTitle("New Template");
                tid.initOwner(getScene().getWindow());
                tid.initModality(Modality.APPLICATION_MODAL);
                ((Stage) tid.getDialogPane().getScene().getWindow()).getIcons().add(Tachyon.icon);
                tid.setHeaderText("Set Template Name");
                Optional<String> name = tid.showAndWait();
                if (name.isPresent()) {
                    if (!name.get().isEmpty()) {
                        ObservableList<String> list = null;
                        try {
                            String s = getSelectedEditor().getCodeArea().getText();
                            int packIndex = s.indexOf("package");
                            String one = s.substring(0, packIndex);
                            String two = s.substring(packIndex);
                            two = two.substring(two.indexOf(";") + 1);
                            int classIndex = two.indexOf("public class");
                            String three = two.substring(classIndex);
                            three = three.substring(three.indexOf("{"));
                            two = two.substring(0, classIndex);
                            list = FXCollections.observableArrayList(one, two, three);
                        } catch (Exception eg) {
                        }
                        if (list != null) {
                            Template.addTemplate(name.get(), list);
                        } else {
                            showAlert(AlertType.ERROR,
                                    getScene().getWindow(),
                                    "New Template",
                                    "Error in Creating New Template", "");
                        }
                    }
                }
            }
        });
        selectTemplate.setOnAction((e) -> {
            if (getCurrentProject() != null) {
                Stage st = new Stage();
                st.setTitle("Select Template");
                st.initModality(Modality.APPLICATION_MODAL);
                ListView<String> options = new ListView<>();
                st.setScene(new Scene(options));
                st.initOwner(getScene().getWindow());
                options.getItems().addAll(Template.getAvailableTemplates());
                st.getIcons().add(Tachyon.icon);
                options.setOnMouseClicked((ex) -> {
                    if (ex.getClickCount() == 2) {
                        if (options.getSelectionModel().getSelectedItem() != null) {
                            String temp = options.getSelectionModel().getSelectedItem();
                            st.close();
                            (new Thread(() -> {
                                try {
                                    Thread.sleep(250);
                                } catch (InterruptedException ef) {
                                }
                                Platform.runLater(() -> {
                                    loadTemplate(temp, getScene().getWindow());
                                });
                            })).start();
                        }
                    }
                });
                st.showAndWait();
            }
        });
        examples.setOnAction((e) -> {
            examples();
        });
        information.setOnAction((e) -> {
            showAlert(AlertType.INFORMATION, getScene().getWindow(),
                    "About",
                    "Tachyon 1.0.0", "Created by Aniket Joshi");
        });
        request.setOnAction((e) -> {
            sendEmail("Feature Request");
        });
        report.setOnAction((e) -> {
            sendEmail("Report Bug");
        });

        projects = new ProjectsView();
        setLeft(projects);
        DragResizeMod.makeResizable(projects.getTreeView());
        TreeView<String> tree = projects.getTreeView();
        tree.setRoot(new TreeItem<>("Projects"));
        tree.getRoot().setExpanded(true);
        ProjectTree.getTree().addListener(new ProjectTree.ProjectTreeListener() {

            @Override
            public void projectAdded(Project added) {
                if (!projectContains(tree.getRoot().getChildren(), added)) {
                    ProjectTreeItem item;
                    tree.getRoot().getChildren().add(item = new ProjectTreeItem(added, true));
                    item.setExpanded(true);
                    added.addListener((new Project.ProjectListener() {

                        @Override
                        public void fileAdded(Project pro, Program add) {
                            if (!scriptContains(item.getChildren().get(0).getChildren(), add)) {
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
                    for (JavaLibrary s : added.getAllLibs()) {
                        item.getChildren().get(1).getChildren().add(new LibraryTreeItem(added, s));
                    }
                    added.setLibraryListener((List<String> filePaths) -> {
                        item.getChildren().get(1).getChildren().clear();
                        for (String s : filePaths) {
                            item.getChildren().get(1).getChildren().add(new LibraryTreeItem(added, new JavaLibrary(s)));
                        }
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
                pro.setLibraryListener(null);
            }
        });

        projects.getTreeView().setOnMouseClicked((e) -> {
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
                                if (ed.getScript() != null) {
                                    if (ed.getScript().equals(sti.getScript())) {
                                        contains = false;
                                        st = ed;
                                    }
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
                    } else if (sel instanceof BinaryTreeItem) {
                        ZipEntry entry = ((BinaryTreeItem) sel).getEntry();
                        if (!entry.isDirectory()) {
                            if (entry.getName().endsWith(".class")) {
                                InputStream is = ((BinaryTreeItem) sel).sourceInputStream();
                                if (is != null) {
                                    addClassReader(((BinaryTreeItem) sel).getProject(), entry.getName().replace(".class", ""), is, -1);
                                }
                            } else {
                                InputStream is = ((BinaryTreeItem) sel).getInputStream();
                                if (is != null) {
                                    addClassReader(((BinaryTreeItem) sel).getProject(), entry.getName().replace(".class", ""), is, -1);
                                }
                            }
                        }
                    }
                }
            }
        });
        tree.getSelectionModel().selectedItemProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                if (newer instanceof DirectoryTreeItem) {
                    currentProject.set(((DirectoryTreeItem) newer).getProject());
                } else if (newer instanceof ProjectTreeItem) {
                    currentProject.set(((ProjectTreeItem) newer).getProject());
                } else if (newer instanceof ProgramTreeItem) {
                    currentProject.set(((ProgramTreeItem) newer).getScript().getProject());
                } else if (newer instanceof LibraryTreeItem) {
                    currentProject.set(((LibraryTreeItem) newer).getProject());
                } else if (newer instanceof BinaryTreeItem) {
                    currentProject.set(((BinaryTreeItem) newer).getProject());
                } else if (newer instanceof PlatformTreeItem) {
                    currentProject.set(((PlatformTreeItem) newer).getProject());
                } else if (newer.getParent() != null) {
                    if (newer.getParent() instanceof ProjectTreeItem) {
                        ProjectTreeItem pti = (ProjectTreeItem) newer.getParent();
                        currentProject.set(pti.getProject());
                    }
                } else {
                    currentProject.set(null);
                }
            }
        });
        tree.setContextMenu(new ContextMenu());
        projects.getTreeView().setOnContextMenuRequested((e) -> {
            TreeItem<String> select = tree.getSelectionModel().getSelectedItem();
            if (select instanceof ProgramTreeItem) {
                ProgramTreeItem sti = (ProgramTreeItem) select;
                MenuItem open, delete, details, count;
                tree.getContextMenu().getItems().setAll(open = new MenuItem("Open File"),
                        delete = new MenuItem("Delete File"),
                        details = new MenuItem("File Details"),
                        count = new MenuItem("Word Count"));
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
                    Optional<ButtonType> show = showAlert(AlertType.CONFIRMATION, getScene().getWindow(),
                            "Confirm", "Are you sure you would like to delete this?", "");
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
                count.setOnAction((fe) -> {
                    WordCount.getWordCount((Stage) getScene().getWindow(), sti.getScript());
                });
            } else if (select instanceof DirectoryTreeItem) {
                tree.getContextMenu().getItems().clear();
            } else if (select instanceof LibraryTreeItem) {
                tree.getContextMenu().getItems().clear();
            } else if (select instanceof PlatformTreeItem) {
                tree.getContextMenu().getItems().clear();
            } else if (select instanceof BinaryTreeItem) {
                tree.getContextMenu().getItems().clear();
            } else if (select instanceof ProjectTreeItem) {
                MenuItem clsepse, delete, reload, prop, details, count;
                tree.getContextMenu().getItems().setAll(clsepse = new MenuItem("Close Project"),
                        delete = new MenuItem("Delete Project"),
                        reload = new MenuItem("Reload Project"),
                        details = new MenuItem("Project Details"),
                        prop = new MenuItem("Properties"),
                        count = new MenuItem("Word Count")
                );
                clsepse.setOnAction((c) -> {
                    closeProject(((ProjectTreeItem) select).getProject());
                });
                delete.setOnAction((fe) -> {
                    Optional<ButtonType> show = showAlert(AlertType.CONFIRMATION,
                            getScene().getWindow(),
                            "Confirm",
                            "Are you sure you would like to delete this?",
                            "");
                    if (show.isPresent()) {
                        if (show.get() == ButtonType.OK) {
                            clsepse.fire();
                            ((ProjectTreeItem) select).getProject().delete();
                        }
                    }
                });
                reload.setOnAction((E) -> {
                    reload(((ProjectTreeItem) select).getProject());
                });
                prop.setOnAction((efd) -> {
                    property(((ProjectTreeItem) select).getProject());
                });
                details.setOnAction((ejgf) -> {
                    Details.getDetails((Stage) getScene().getWindow(), ((ProjectTreeItem) select).getProject().getRootDirectory()).showAndWait();
                });
                count.setOnAction((efd) -> {
                    WordCount.getWordCount((Stage) getScene().getWindow(), ((ProjectTreeItem) select).getProject());
                });
            } else {
                tree.getContextMenu().getItems().clear();
            }
            resizeMenuItems(tree.getContextMenu().getItems(), "-fx-font-size:" + fontSize.get().getSize());
        });
        resize(bar, "-fx-font-size:15");
        fontSize.addListener((ob, older, newer) -> {
            resize(bar, "-fx-font-size:" + newer.getSize());
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener((ob, older, newer) -> {
            if (newer instanceof EnvironmentTab) {
                EnvironmentTab et = (EnvironmentTab) newer;
                currentProject.set(et.getScript().getProject());
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
            if (c.getList().size() == 0) {
                bottom.setPrefHeight(0);
            } else {
                bottom.setPrefHeight(300);
            }
        });
        setBottom(bottom);
        bottom.setCenter(console);
        bottom.setPadding(new Insets(5, 10, 5, 10));
        tabPane.getTabs().add(0, new Welcome());
        tabPane.getSelectionModel().select(0);
    }

    private class Welcome extends Tab {

        private final Label center, top;
        private final VBox options;
        private final Hyperlink newProject, openProject, openFile, aboutApp;

        public Welcome() {
            setText("Start Page");
            setContent(new BorderPane(center = new Label("Welcome to Tachyon!"),
                    null,
                    null,
                    null,
                    options = new VBox(15)));
            options.getChildren().addAll(
                    top = new Label("Options"),
                    newProject = new Hyperlink("New Project"),
                    openProject = new Hyperlink("Open Project"),
                    openFile = new Hyperlink("Open File"),
                    aboutApp = new Hyperlink("About Tachyon"));
            options.setPadding(new Insets(5, 10, 5, 10));
            options.setAlignment(Pos.TOP_CENTER);
            top.setFont(new Font(20));
            options.setStyle("-fx-background-color:white;");
            for (Node n : options.getChildren()) {
                if (n instanceof Hyperlink) {
                    Hyperlink h = (Hyperlink) n;
                    h.setFont(new Font(20));
                }
            }
            newProject.setOnAction((e) -> {
                Writer.this.nProject.fire();
                newProject.setVisited(false);
            });
            openProject.setOnAction((e) -> {
                Writer.this.oProject.fire();
                openProject.setVisited(false);
            });
            openFile.setOnAction((e) -> {
                Writer.this.oFile.fire();
                openFile.setVisited(false);
            });
            aboutApp.setOnAction((e) -> {
                Writer.this.information.fire();
                aboutApp.setVisited(false);
            });
            center.setFont(new Font(30));
        }
    }

    private void loadTemplate(String temp, Window st) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.initOwner(st);
        dialog.setTitle("Class Name");
        dialog.setHeaderText("Enter Package Name and Class Name");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Package");
        TextField password = new TextField();
        password.setPromptText("Class");

        grid.add(new Label("Package:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Class:"), 0, 1);
        grid.add(password, 1, 1);

        ButtonType loginButtonType = new ButtonType("Finish", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> username.requestFocus());
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (!result.get().getValue().isEmpty()) {
                String cName;
                if (!result.get().getKey().isEmpty()) {
                    cName = result.get().getKey() + "." + result.get().getValue();
                } else {
                    cName = result.get().getValue();
                }
                System.out.println(Program.getFilePath(cName));
                String totalPath = getCurrentProject().getSource().toAbsolutePath().toString() + File.separator + Program.getFilePath(cName) + ".java";
                System.out.println("Total : " + totalPath);
                File f = new File(totalPath);
                if (f.exists()) {
                    showAlert(AlertType.WARNING, 
                            getScene().getWindow(), 
                            "File Exists", 
                            "Unfortunately this file already exists", 
                            "");
                } else {
                    Program pro = new Program(Program.JAVA, cName, f.toPath(), Template.getTemplateCode(temp, result.get().getKey(), result.get().getValue()), getCurrentProject());
                    getCurrentProject().addScript(pro);
                    loadFile(pro.getFile().toFile(), pro, pro.getProject());
                }
            }
        }
    }

    private void sendEmail(String subject) {
        File f = new File(".cache" + File.separator + "emailstamp.txt");
        if (f.exists()) {
            ArrayList<String> al = new ArrayList<>();
            try {
                al.addAll(Files.readAllLines(f.toPath()));
            } catch (IOException ex) {
            }
            LocalDate ld = LocalDate.parse(al.get(0));
            LocalTime lt = LocalTime.parse(al.get(1));
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            if (today.isEqual(ld)) {
                if (now.isAfter(lt)) {
                    if (now.getHour() > lt.getHour() + 1) {
                        confirmSend(subject);
                    } else {
                        unableToSend();
                    }
                } else {
                    unableToSend();
                }
            } else if (today.isAfter(ld)) {
                confirmSend(subject);
            } else {
                unableToSend();
            }
        } else {
            confirmSend(subject);
        }
    }

    public final void examples() {
        Stage st = new Stage();
        st.initOwner(getScene().getWindow());
        st.initModality(Modality.APPLICATION_MODAL);
        st.setTitle("Examples");
        st.getIcons().add(Tachyon.icon);
        st.setResizable(false);
        ListView<String> lv = new ListView<>();
        BorderPane main = new BorderPane();
        main.setTop(new Label("Select a Sample"));
        BorderPane.setAlignment(main.getTop(), Pos.CENTER);
        st.setScene(new Scene(main));
        main.setPadding(new Insets(5, 10, 5, 10));
        main.setCenter(lv);
        lv.getItems().addAll(Examples.getExamples().getAllExamples());
        lv.setOnMouseClicked((ex) -> {
            if (ex.getClickCount() == 2) {
                if (lv.getSelectionModel().getSelectedItem() != null) {
                    String temp = lv.getSelectionModel().getSelectedItem();
                    st.close();
                    (new Thread(() -> {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ef) {
                        }
                        Platform.runLater(() -> {
                            loadSample(temp, getScene().getWindow(), lv.getItems().indexOf(temp));
                        });
                    })).start();
                }
            }
        });
        st.showAndWait();
    }

    private void loadSample(String example, Window w, int index) {
        File f = new File(Dependencies.workplace_location + File.separator + example);
        int x = 1;
        while (f.exists()) {
            f = new File(Dependencies.workplace_location + File.separator + example + x);
            x++;
        }
        String mainClass;
        if (index == 0) {
            mainClass = "user.input.Launcher";
        } else {
            mainClass = "web.browser.Launcher";
        }
        Project pro = new Project(f.toPath(), mainClass, true, 0);
        pro.getPrograms().get(0).save(Examples.getExamples().getCode(index));
        ProjectTree.getTree().addProject(pro);
    }

    private void unableToSend() {
        showAlert(AlertType.ERROR, getScene().getWindow(), "Unable to Send", 
                "Cannot send another email so soon after the last one!", "");
    }

    private void confirmSend(String sub) {
        new EmailPicker(getScene().getWindow(), sub).showAndWait();
    }

    private void reload(Project pro) {
        closeProject(pro);
        String main = pro.getMainClassName();
        ProjectTree.getTree().addProject(new Project(pro.getRootDirectory(), main, false));
    }

    private void findScriptTreeItem(ProjectTreeItem pro, Program scr) {
        if (pro instanceof DirectoryTreeItem) {
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
        } else {
            for (int x = pro.getChildren().get(0).getChildren().size() - 1; x >= 0; x--) {
                TreeItem<String> al = pro.getChildren().get(0).getChildren().get(x);
                if (al instanceof ProgramTreeItem) {
                    ProgramTreeItem sti = (ProgramTreeItem) al;
                    if (sti.getScript().equals(scr)) {
                        pro.getChildren().get(0).getChildren().remove(sti);
                    }
                } else if (al instanceof DirectoryTreeItem) {
                    DirectoryTreeItem dir = (DirectoryTreeItem) al;
                    findScriptTreeItem(dir, scr);
                }
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
                    if (pti instanceof DirectoryTreeItem) {
                        pti.getChildren().add(dti);
                    } else {
                        pti.getChildren().get(0).getChildren().add(dti);
                    }
                }
                left = left.substring(left.indexOf(File.separator) + 1);
                pti = dti;
                last = dti;
            }
            if (last != null) {
                last.getChildren().add(sti);
            }
        } else {
            pti.getChildren().get(0).getChildren().add(sti);
        }
    }

    private DirectoryTreeItem getDirectoryItem(ProjectTreeItem pro, String name) {
        if (pro instanceof DirectoryTreeItem) {
            for (TreeItem<String> tri : pro.getChildren()) {
                if (tri instanceof DirectoryTreeItem) {
                    if (tri.getValue().equals(name)) {
                        return (DirectoryTreeItem) tri;
                    } else {
                        return getDirectoryItem((DirectoryTreeItem) tri, name);
                    }
                }
            }
        } else {
            for (TreeItem<String> tri : pro.getChildren().get(0).getChildren()) {
                if (tri instanceof DirectoryTreeItem) {
                    if (tri.getValue().equals(name)) {
                        return (DirectoryTreeItem) tri;
                    } else {
                        return getDirectoryItem((DirectoryTreeItem) tri, name);
                    }
                }
            }
        }

        return null;
    }

    public final Project getCurrentProject() {
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

    public void loadFiles(List<File> fil) {
        fil.stream().forEach((f) -> {
            loadFile(f, null);
        });
    }

    public void loadFiles(String[] arr) {
        ArrayList<File> al = new ArrayList<>();
        for (String s : arr) {
            al.add(new File(s));
        }
        loadFiles(al);
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
                System.out.println(type);
                if (fileSize(f)) {
                    if (type != null) {
                        if (type.contains("text")) {
                            Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                            addTab(new Editor(pro, parent));
                        } else if (type.contains("image")) {
                            Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                            addTab(new Viewer(pro, parent));
                        } else if (type.contains("fxml")) {
                            Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                            addTab(new FXMLTab(pro, parent));
                        } else {
                            if (alert(f.getAbsolutePath())) {
                                Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                                addTab(new Editor(pro, parent));
                            }
                        }
                    } else {
                        System.out.println(f.getName());
                        if (f.getName().endsWith(".pdf")) {
                            Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                            addTab(new PdfReader(pro, parent));
                        } else if (alert(f.getAbsolutePath())) {
                            Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                            addTab(new Editor(pro, parent));
                        }
                    }
                }
            }
        } else {
            String type = "";
            try {
                type = Files.probeContentType(prog.getFile());
            } catch (IOException ef) {
            }
            System.out.println(type);
            if (fileSize(prog.getFile().toFile())) {
                if (type != null) {
                    if (type.contains("text")) {
                        addTab(new Editor(prog, prog.getProject()));
                    } else if (type.contains("image")) {
                        addTab(new Viewer(prog, prog.getProject()));
                    } else if (type.contains("fxml")) {
                        addTab(new FXMLTab(prog, prog.getProject()));
                    } else {
                        if (alert(prog.getFile().toAbsolutePath().toString())) {
                            addTab(new Editor(prog, prog.getProject()));
                        }
                    }
                } else {
                    if (prog.getFile().toString().endsWith(".pdf")) {
                        Program pro = new Program(Program.RESOURCE, f.toPath(), new ArrayList<>(), parent);
                        addTab(new PdfReader(pro, parent));
                    } else if (alert(prog.getFile().toAbsolutePath().toString())) {
                        addTab(new Editor(prog, prog.getProject()));
                    }
                }
            }
        }
    }

    private boolean fileSize(File f) {
        if (getScene() == null) {
            return true;
        }
        if (getScene().getWindow() == null) {
            return true;
        }
        if (f.length() > (1024 * 1024)) {
            Optional<ButtonType> show = showAlert(AlertType.CONFIRMATION,getScene().getWindow(),"File Size",f.getName() + "'s file size is " + getFileSize(f.length()) + " MB."
                    + "\nIf you continue, Tachyon may throw an OutOfMemoryException\n"
                    + "and become unusable","");            
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private double getFileSize(long l) {
        return l / (double) (1024 * 1024);
    }

    private void addTab(EnvironmentTab tab) {
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
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

    private boolean alert(String s) {
        if (s.endsWith(".java")
                || s.endsWith(".fxml")
                || s.endsWith(".c")
                || s.endsWith(".h")) {
            return true;
        }
        if (getScene() == null) {
            return true;
        }
        if (getScene().getWindow() == null) {
            return true;
        }
        Optional<ButtonType> show =showAlert(AlertType.ERROR,getScene().getWindow(),"File Type","Tachyon is unable to read this type of File","Would you still like to open this File?");
        if (show.isPresent()) {
            if (show.get() == ButtonType.OK) {
                return true;
            }
        }
        return false;
    }

    private void resize(MenuBar bar, String style) {
        resizeMenus(bar.getMenus(), style);
    }

    private void resizeMenus(ObservableList<Menu> me, String style) {
        me.stream().forEach((m) -> {
            resizeMenuItems(m.getItems(), style);
            m.setStyle(style);
        });
        projects.getTreeView().setStyle(style);
    }

    private void resizeMenuItems(ObservableList<MenuItem> me, String style) {
        me.stream().forEach((m) -> {
            m.setStyle(style);
        });
        projects.getTreeView().setStyle(style);
    }

    public boolean processCheck() {
        int size = ProcessPool.getPool().size();
        if (size == 0) {
            return true;
        } else {
            Optional<ButtonType> show = showAlert(Alert.AlertType.CONFIRMATION,getScene().getWindow(),"Running Processes",ProcessPool.getPool().getAccumulatedText(),
                    "These processes are currently running!\n"
                    + "Would you like to cancel all of the processes?");
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    ProcessPool.getPool().cancel();
                    return true;
                }
            }
            return false;
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
            boolean b = new PrinterDialog(getScene().getWindow(), getSelectedTab().getContent()).showAndWait();
            if (b) {
                showAlert(AlertType.INFORMATION,getScene().getWindow(), "Print Successful!","Print Starting!", "");
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
                if (!ProjectTree.getTree().getProjects().contains(pro)) {
                    ProjectTree.getTree().addProject(pro);
                }
            }
        }
    }

    public final void newFile() {
        if (getCurrentProject() != null) {
            Optional<FileDescription> show = new FileWizard(getScene().getWindow(), getCurrentProject()).showAndWait();
            if (show.isPresent()) {
                Program sc;
                if (show.get().getDescription().contains("Other")) {
                    String extension = show.get().getName().substring(show.get().getName().lastIndexOf('.'));
                    sc = new Program(Program.RESOURCE,
                            Paths.get(getCurrentProject().getSource().toAbsolutePath().toString() + File.separatorChar + Program.getFilePath(show.get().getName().substring(0, show.get().getName().lastIndexOf('.'))) + extension),
                            FileWizard.getTemplateCode(show.get().getDescription(),
                                    show.get().getName()),
                            getCurrentProject());
                    getCurrentProject().addScript(sc);
                    loadFile(sc.getFile().toFile(), sc, sc.getProject());
                } else if (show.get().getDescription().contains("FXML")
                        || show.get().getDescription().contains("HTML")
                        || show.get().getDescription().contains("Text")) {
                    String extension;
                    if (show.get().getDescription().contains("FXML")) {
                        extension = ".fxml";
                    } else if (show.get().getDescription().contains("HTML")) {
                        extension = ".html";
                    } else {
                        extension = ".txt";
                    }
                    sc = new Program(Program.RESOURCE,
                            Paths.get(getCurrentProject().getSource().toAbsolutePath().toString() + File.separatorChar + Program.getFilePath(show.get().getName()) + extension),
                            FileWizard.getTemplateCode(show.get().getDescription(),
                                    show.get().getName()),
                            getCurrentProject());
                    getCurrentProject().addScript(sc);
                    loadFile(sc.getFile().toFile(), sc, sc.getProject());
                } else {
                    sc = new Program(Program.JAVA,
                            show.get().getName(),
                            Paths.get(getCurrentProject().getSource().toAbsolutePath().toString() + File.separator + Program.getFilePath(show.get().getName()) + ".java"),
                            FileWizard.getTemplateCode(show.get().getDescription(),
                                    show.get().getName()),
                            getCurrentProject());
                    getCurrentProject().addScript(sc);
                    Editor ed;
                    tabPane.getTabs().add(ed = new Editor(sc, sc.getProject()));
                    tabPane.getSelectionModel().select(ed);
                }

            }
        } else {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Save Location");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java File", "*.java"));
            File f = fc.showSaveDialog(getScene().getWindow());
            if (f != null) {
                if (!f.getAbsolutePath().endsWith(".java")) {
                    f = new File(f.getAbsolutePath() + ".java");
                }
                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.initOwner(getScene().getWindow());
                dialog.setTitle("Class Name");
                dialog.setHeaderText("Enter Package Name and Class Name");

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                TextField username = new TextField();
                username.setPromptText("Package");
                TextField password = new TextField();
                password.setPromptText("Class");

                grid.add(new Label("Package:"), 0, 0);
                grid.add(username, 1, 0);
                grid.add(new Label("Class:"), 0, 1);
                grid.add(password, 1, 1);

                ButtonType loginButtonType = new ButtonType("Finish", ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

                dialog.getDialogPane().setContent(grid);
                Platform.runLater(() -> username.requestFocus());
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == loginButtonType) {
                        return new Pair<>(username.getText(), password.getText());
                    }
                    return null;
                });
                Optional<Pair<String, String>> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String key = result.get().getKey() == null ? "" : result.get().getKey();
                    String val = result.get().getValue() == null ? "" : result.get().getValue();
                    if (!val.isEmpty()) {
                        Program pro = new Program(Program.RESOURCE, f.toPath(), Template.getTemplateCode("Java Main Class", key, val), null);
                        loadFile(pro.getFile().toFile(), pro, null);
                    } else {
                        showAlert(AlertType.INFORMATION,
                                getScene().getWindow(),
                                "New File",
                                "No Class Name Specified", "");
                    }
                }
            }
        }
    }

    public final void newProject() {
        ProjectOptions opt = new ProjectOptions(getScene().getWindow());
        int show = opt.showAndWait();
        if (show != -1) {
            System.out.println(show);
            Project pro = ProjectWizard.createProject(getScene().getWindow(), show);
            if (pro != null) {
                ArrayList<Program> scripts = pro.getPrograms();
                for (int x = 0; x < scripts.size(); x++) {
                    Editor ed = new Editor(scripts.get(x), pro);
                    tabPane.getTabs().add(ed);
                }
                ProjectTree.getTree().addProject(pro);
            }
        }
    }

    public final void clean() {
        if (getCurrentProject() != null) {
            getCurrentProject().clean();
            build.fire();
        }
    }

    public final void run() {
        saveAll();
        if (getCurrentProject() != null) {
            ProcessItem pro = new ProcessItem(null, null, new Console(getCurrentProject()));
            addConsoleWindow(pro);
            Task<Void> tk = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    getCurrentProject().run(pro);
                    return null;
                }

            };
            (new Thread(tk)).start();
        } else {
            runFile();
        }
    }

    public final void debug() {
        saveAll();
        if (getCurrentProject() != null) {
            ProcessItem pro = new ProcessItem(null, null, new Console(getCurrentProject()));
            addConsoleWindow(pro);
            DebuggerController con = new DebuggerController();
            DebuggerConsole debugCon = new DebuggerConsole(this, con);
            setRight(debugCon);
            Task<Void> tk = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    getCurrentProject().debugProject(pro, con);
                    return null;
                }
            };
            (new Thread(tk)).start();
        }
    }

    public final void runFile() {
        saveAll();
        if (getSelectedEditor() != null) {
            if (getSelectedEditor().getScript().getProject() != null) {
                runFile(getSelectedEditor().getScript().getProject(), getSelectedEditor().getScript());
            } else {
                if (getSelectedEditor().getScript().getFile().getFileName().toString().endsWith(".java")) {
                    runFile(getSelectedEditor().getScript());
                }
            }
        }
    }

    private void runFile(Program pro) {
        ProcessItem item = new ProcessItem(null, null, new Console(null));
        addConsoleWindow(item);
        Task<Void> tk = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                TaskManager.runIsolatedFile(item, pro);
                return null;
            }

        };
        (new Thread(tk)).start();
    }

    private void runFile(Project projec, Program pro) {
        ProcessItem item = new ProcessItem(null, null, new Console(projec));
        addConsoleWindow(item);
        Task<Void> tk = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                projec.runFile(item, pro);
                return null;
            }

        };
        (new Thread(tk)).start();
    }

    public final void fatJar() {
        if (getCurrentProject() != null) {
            getCurrentProject().clean();
            saveAll();
            ProcessItem pro = new ProcessItem(null, null, new Console(getCurrentProject()));
            addConsoleWindow(pro);
            Task<Void> tk = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    getCurrentProject().fatJar(pro);
                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION,
                                getScene().getWindow(),
                                "Deploy",
                                "Created Jar",
                                "bundle.jar placed in " + getCurrentProject().getDist().toAbsolutePath().toString());
                    });
                    return null;
                }

            };
            (new Thread(tk)).start();

        }
    }

    public final void build() {
        saveAll();
        if (getCurrentProject() != null) {
            ProcessItem pro = new ProcessItem(null, null, new Console(getCurrentProject()));
            addConsoleWindow(pro);
            Task<Void> tk = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    getCurrentProject().build(pro);
                    return null;
                }

            };
            (new Thread(tk)).start();
        }
    }

    public final void closeProject() {
        if (getCurrentProject() != null) {
            closeProject(getCurrentProject());
        }
    }

    public final void launchJar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Jar File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Jar File", "*.jar"));
        File id = fc.showOpenDialog(getScene().getWindow());
        if (id != null) {
            ProcessItem pro = new ProcessItem(null, null, new Console(getCurrentProject()));
            addConsoleWindow(pro);
            Task<Void> tk = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    TaskManager.launchJar(pro, id);
                    return null;
                }

            };
            (new Thread(tk)).start();
        }
    }

    private void closeProject(Project pro) {
        ProjectTree.getTree().removeProject(pro);
        currentProject.set(null);
    }

    public final void closeAllProjects() {
        for (int x = ProjectTree.getTree().getProjects().size() - 1; x >= 0; x--) {
            closeProject(ProjectTree.getTree().getProjects().get(x));
        }
    }

    public final void executable() {
        if (getCurrentProject() != null) {
            boolean b = verifyDependencies();
            if (b) {
                saveAll();
                ProcessItem pro = new ProcessItem(null, null, new Console(getCurrentProject()));
                addConsoleWindow(pro);
                Task<Void> tk = new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        getCurrentProject().nativeExecutable(pro);
                        return null;
                    }

                };
                (new Thread(tk)).start();
            } else {
                Stage sta = new Stage();
                sta.setTitle("Inno Setup");
                sta.initOwner(getScene().getWindow());
                sta.getIcons().add(Tachyon.icon);
                sta.initModality(Modality.APPLICATION_MODAL);
                sta.setScene(new Scene(new NativeInformation()));
                sta.showingProperty().addListener((ob, older, newer) -> {
                    if (newer) {
                        showAlert(AlertType.ERROR,
                                sta,
                                "Native Deploy",
                                "Inno Setup Environment Variable has not been set!",
                                "Follow these steps to install it correctly");
                    }
                });
                sta.showAndWait();
            }
        }
    }

    private boolean verifyDependencies() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String env = System.getenv("PATH");
            return env.contains("Inno Setup 5");
        }
        return true;
    }

    public final void zip() {
        String out = getCurrentProject().getDist().toAbsolutePath().toString()
                + File.separator + getCurrentProject().getProjectName() + ".zip";
        ZipUtils appZip = new ZipUtils();
        String sourceFile = getCurrentProject().getSource().toAbsolutePath().toString();
        appZip.generateFileList(new File(sourceFile), sourceFile);
        appZip.zipIt(sourceFile, out);
        showAlert(AlertType.INFORMATION,
                getScene().getWindow(),
                "Zip Deploy",
                "Zip File has been created and placed in Directory : " + getCurrentProject().getDist().toAbsolutePath().toString(),
                "");
    }

    public final void projectProperties() {
        if (getCurrentProject() != null) {
            property(getCurrentProject());
        }
    }

    private void property(Project prop) {
        if (getCurrentProject() != null) {
            new ProjectProperties(prop, getScene().getWindow()).showAndWait();
        }
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
        ObservableList<TreeItem<String>> children = projects.getTreeView().getRoot().getChildren();
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
            if (b instanceof EnvironmentTab && !(b instanceof ClassReader)) {
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

    private void evaluate(KeyEvent kc) {
        if (kc.isControlDown()) {
            if (kc.isShiftDown()) {
                if (kc.getCode() == KeyCode.N) {
                    nProject.fire();
                }
                if (kc.getCode() == KeyCode.O) {
                    oProject.fire();
                }
                if (kc.getCode() == KeyCode.S) {
                    saveAll.fire();
                }
                if (kc.getCode() == KeyCode.C) {
                    cProject.fire();
                }
            } else {
                if (kc.getCode() == KeyCode.N) {
                    nFile.fire();
                }
                if (kc.getCode() == KeyCode.Z) {
                    undo.fire();
                }
                if (kc.getCode() == KeyCode.Y) {
                    redo.fire();
                }
                if (kc.getCode() == KeyCode.X) {
                    cut.fire();
                }
                if (kc.getCode() == KeyCode.C) {
                    copy.fire();
                }
                if (kc.getCode() == KeyCode.V) {
                    paste.fire();
                }
                if (kc.getCode() == KeyCode.S) {
                    save.fire();
                }
                if (kc.getCode() == KeyCode.O) {
                    oFile.fire();
                }
                if (kc.getCode() == KeyCode.A) {
                    selectAll.fire();
                }
            }
        } else {
        }
    }

    final void addEditorFromStackTrace(Program p, int line) {
        Editor ed = null;
        for (Tab b : tabPane.getTabs()) {
            if (b instanceof Editor) {
                Editor eda = (Editor) b;
                if (eda.getScript().equals(p)) {
                    ed = eda;
                }
            }
        }
        if (ed == null) {
            ed = new Editor(p, p.getProject());
            tabPane.getTabs().add(ed);
        }
        tabPane.getSelectionModel().select(ed);
        select(ed, line);
    }

    final void addClassReader(Project pro, String name, InputStream stream, int line) {
        if (stream != null) {
            ClassReader cr = null;
            for (Tab b : tabPane.getTabs()) {
                if (b instanceof ClassReader) {
                    ClassReader cra = (ClassReader) b;
                    if (cra.getGraph().getText().contains(name)) {
                        cr = cra;
                    }
                }
            }
            if (cr == null) {
                cr = new ClassReader(null, pro, name + ".class", stream);
                tabPane.getTabs().add(cr);
            }
            tabPane.getSelectionModel().select(cr);
            if (line != -1) {
                select(cr, line);
            }
        }
    }

    private void select(Editor ed, int line) {
        int caret = 0;
        int selectFinal = 0;
        for (int x = 0; x < line - 1; x++) {
            Paragraph<Collection<String>> pcs = ed.getCodeArea().getParagraph(x);
            caret += pcs.toString().length() + 1;
        }
        selectFinal += caret;
        selectFinal += ed.getCodeArea().getParagraph(line - 1).toString().length();
        ed.getCodeArea().selectRange(caret, selectFinal);
        ed.getCodeArea().requestFocus();
    }

    private void select(ClassReader ed, int line) {
        int caret = 0;
        int selectFinal = 0;
        for (int x = 0; x < line - 1; x++) {
            Paragraph<Collection<String>> pcs = ed.getCodeArea().getParagraph(x);
            caret += pcs.toString().length() + 1;
        }
        selectFinal += caret;
        selectFinal += ed.getCodeArea().getParagraph(line - 1).toString().length();
        ed.getCodeArea().selectRange(caret, selectFinal);
        ed.getCodeArea().requestFocus();
    }

    public static Optional<ButtonType> showAlert(AlertType al, Window w, String title, String head, String cont) {
        Alert ale = new Alert(al);
        ale.initOwner(w);
        ale.setTitle(title);
        ale.setHeaderText(head);
        ale.setContentText(cont);
        return ale.showAndWait();
    }

}
