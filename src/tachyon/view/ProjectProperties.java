/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;
import tachyon.core.JavaLibrary;
import tachyon.core.JavaProgram;
import tachyon.core.JavaProject;
import tachyon.core.Program;

/**
 *
 * @author Aniket
 */
public class ProjectProperties {

    private final Stage stage;
    private final TabPane pane;
    private final VBox box1, box2, box3;
    private final TextField mainClass;
    private final Button select, confirm, cancel;
    private final JavaProject project;

    private final ListView<String> libsView;
    private final Button addJar, removeJar;

    public ProjectProperties(JavaProject project, Window w) {
        this.project = project;
        stage = new Stage();
        stage.initOwner(w);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setWidth(600);
        stage.setTitle("Project Properties - " + project.getProjectName());
        stage.getIcons().add(tachyon.Tachyon.icon);
        stage.setResizable(false);
        HBox mai, libs, one, two, thr, fou;
        ListView<String> compileList, runtimeList;
        Button compileAdd, compileRemove, preview, selectIm, runtimeAdd, runtimeRemove;
        TextField iconField;
        stage.setScene(new Scene(new VBox(5, pane = new TabPane(
                new Tab("Library Settings",
                        box1 = new VBox(15,
                                libs = new HBox(10, new Label("External Libraries"),
                                        libsView = new ListView<>(),
                                        new VBox(5,
                                                addJar = new Button("Add Jar"),
                                                removeJar = new Button("Remove Jar")))
                        )),
                new Tab("Program Settings",
                        new ScrollPane(
                                box2 = new VBox(15,
                                        one = new HBox(10, new Label("Main-Class"),
                                                mainClass = new TextField(project.getMainClassName()),
                                                select = new Button("Select")),
                                        two = new HBox(10, new Label("Compile-Time Arguments"),
                                                compileList = new ListView<>(),
                                                new VBox(5,
                                                        compileAdd = new Button("Add Argument"),
                                                        compileRemove = new Button("Remove Argument")))
                                ))),
                new Tab("Deployment Settings",
                        box3 = new VBox(15,
                                thr = new HBox(10,
                                        new Label("Icon File"),
                                        iconField = new TextField(project.getFileIconPath()),
                                        preview = new Button("Preview Image"),
                                        selectIm = new Button("Select Icon")),
                                fou = new HBox(10, new Label("Runtime Arguments"),
                                        runtimeList = new ListView<>(),
                                        new VBox(5,
                                                runtimeAdd = new Button("Add Argument"),
                                                runtimeRemove = new Button("Remove Argument")))))
        ),
                new VBox(15,
                        mai = new HBox(10,
                                cancel = new Button("Cancel"),
                                confirm = new Button("Confirm")))
        ))
        );
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        for (Tab b : pane.getTabs()) {
            b.setClosable(false);
        }
        mainClass.setPromptText("Main-Class");
        box1.setPadding(new Insets(5, 10, 5, 10));
        mai.setAlignment(Pos.CENTER_RIGHT);
        mai.setPadding(box1.getPadding());
        libs.setAlignment(Pos.CENTER);
        one.setAlignment(Pos.CENTER);
        two.setAlignment(Pos.CENTER);
        thr.setAlignment(Pos.CENTER);
        fou.setAlignment(Pos.CENTER);
        box1.setAlignment(Pos.CENTER);
        box2.setPadding(box1.getPadding());
        box2.setAlignment(Pos.CENTER);
        box3.setPadding(box1.getPadding());
        box3.setAlignment(Pos.CENTER);
        mainClass.setEditable(false);
        mainClass.setPrefWidth(200);
        for (JavaLibrary lib : project.getAllLibs()) {
            libsView.getItems().add(lib.getBinaryAbsolutePath());
        }
        for (String sa : project.getCompileTimeArguments().keySet()) {
            compileList.getItems().add(sa + ":" + project.getCompileTimeArguments().get(sa));
        }
        for (String sa : project.getRuntimeArguments()) {
            runtimeList.getItems().add(sa);
        }
        compileAdd.setOnAction((e) -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Compiler Argument");
            dialog.initOwner(stage);
            dialog.setHeaderText("Entry the argument");

            ButtonType loginButtonType = new ButtonType("Done", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField username = new TextField();
            username.setPromptText("Key");
            TextField password = new TextField();
            password.setPromptText("Value");

            grid.add(new Label("Key:"), 0, 0);
            grid.add(username, 1, 0);
            grid.add(new Label("Value:"), 0, 1);
            grid.add(password, 1, 1);

            Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
            loginButton.setDisable(true);

            username.textProperty().addListener((observable, oldValue, newValue) -> {
                loginButton.setDisable(newValue.trim().isEmpty());
            });

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
                compileList.getItems().add(result.get().getKey() + ":" + result.get().getValue());
            }
        });
        compileRemove.setOnAction((e) -> {
            if (compileList.getSelectionModel().getSelectedItem() != null) {
                compileList.getItems().remove(compileList.getSelectionModel().getSelectedItem());
            }
        });
        runtimeAdd.setOnAction((e) -> {
            TextInputDialog dia = new TextInputDialog();
            dia.setTitle("Add Runtime Argument");
            dia.setHeaderText("Enter an argument");
            dia.initOwner(stage);
            Optional<String> res = dia.showAndWait();
            if (res.isPresent()) {
                runtimeList.getItems().add(res.get());
            }
        });
        runtimeRemove.setOnAction((e) -> {
            if (runtimeList.getSelectionModel().getSelectedItem() != null) {
                runtimeList.getItems().remove(runtimeList.getSelectionModel().getSelectedItem());
            }
        });
        preview.setOnAction((e) -> {
            if (!iconField.getText().isEmpty()) {
                if (iconField.getText().endsWith(".ico")) {
                    List<BufferedImage> read = new ArrayList<>();
                    try {
                        read.addAll(ICODecoder.read(new File(iconField.getText())));
                    } catch (IOException ex) {
                    }
                    if (read.size() >= 1) {
                        Image im = SwingFXUtils.toFXImage(read.get(0), null);
                        Stage st = new Stage();
                        st.initOwner(stage);
                        st.initModality(Modality.APPLICATION_MODAL);
                        st.setTitle("Icon Preview");
                        st.getIcons().add(Tachyon.icon);
                        st.setScene(new Scene(new BorderPane(new ImageView(im))));
                        if (applyCss.get()) {
                            st.getScene().getStylesheets().add(css);
                        }
                        st.showAndWait();
                    }
                } else if (iconField.getText().endsWith(".icns")) {
                    try {
                        BufferedImage ima = Imaging.getBufferedImage(new File(iconField.getText()));
                        if (ima != null) {
                            Image im = SwingFXUtils.toFXImage(ima, null);
                            Stage st = new Stage();
                            st.initOwner(stage);
                            st.initModality(Modality.APPLICATION_MODAL);
                            st.setTitle("Icon Preview");
                            st.getIcons().add(Tachyon.icon);
                            st.setScene(new Scene(new BorderPane(new ImageView(im))));
                            if (applyCss.get()) {
                                st.getScene().getStylesheets().add(css);
                            }
                            st.showAndWait();
                        }
                    } catch (ImageReadException | IOException ex) {
                    }
                } else {
                    Image im = new Image(new File(iconField.getText()).toURI().toString());
                    Stage st = new Stage();
                    st.initOwner(stage);
                    st.initModality(Modality.APPLICATION_MODAL);
                    st.setTitle("Icon Preview");
                    st.getIcons().add(Tachyon.icon);
                    st.setScene(new Scene(new BorderPane(new ImageView(im))));
                    if (applyCss.get()) {
                        st.getScene().getStylesheets().add(css);
                    }
                    st.showAndWait();
                }
            }
        });
        selectIm.setOnAction((e) -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Icon File");
            String OS = System.getProperty("os.name").toLowerCase();
            fc.getExtensionFilters().add(new ExtensionFilter("Icon File", OS.contains("win") ? getWindowsImageExtensions() : getMacImageExtensions()));
            fc.setSelectedExtensionFilter(fc.getExtensionFilters().get(0));
            File lf = fc.showOpenDialog(stage);
            if (lf != null) {
                iconField.setText(lf.getAbsolutePath());
            }
        });

        addJar.setOnAction((e) -> {
            FileChooser f = new FileChooser();
            f.setTitle("External Libraries");
            f.setSelectedExtensionFilter(new ExtensionFilter("Jar Files", "*.jar"));
            List<File> showOpenMultipleDialog = f.showOpenMultipleDialog(stage);
            if (showOpenMultipleDialog != null) {
                for (File fi : showOpenMultipleDialog) {
                    if (!libsView.getItems().contains(fi.getAbsolutePath())) {
                        libsView.getItems().add(fi.getAbsolutePath());
                    }
                }
            }
        });

        removeJar.setOnAction((e) -> {
            String selected = libsView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                libsView.getItems().remove(selected);
            }
        });

        select.setOnAction((e) -> {
            List<String> all = getAll();
            ChoiceDialog<String> di = new ChoiceDialog<>(project.getMainClassName(), all);
            di.setTitle("Select Main Class");
            di.initOwner(stage);
            di.setHeaderText("Select A Main Class");
            Optional<String> show = di.showAndWait();
            if (show.isPresent()) {
                mainClass.setText(show.get());
            }
        });
        cancel.setOnAction((e) -> {
            stage.close();
        });
        confirm.setOnAction((e) -> {
            project.setMainClassName(mainClass.getText());
            project.setFileIconPath(iconField.getText());
            project.setRuntimeArguments(runtimeList.getItems());
            HashMap<String, String> al = new HashMap<>();
            for (String s : compileList.getItems()) {
                String[] spl = s.split(":");
                al.put(spl[0], spl[1]);
            }
            project.setCompileTimeArguments(al);
            project.setAllLibs(libsView.getItems());
            stage.close();
        });
    }

    private List<String> getWindowsImageExtensions() {
        List<String> im = getCommonImageExtensions();
        im.add("*.ico");
        return im;
    }

    private List<String> getMacImageExtensions() {
        List<String> im = getCommonImageExtensions();
        im.add("*.icns");
        return im;
    }

    private List<String> getCommonImageExtensions() {
        ArrayList<String> al = new ArrayList<>();
        for (String s : ImageIO.getReaderFormatNames()) {
            al.add("*." + s);
        }
        return al;
    }

    private List<String> getAll() {
        ArrayList<String> al = new ArrayList<>();
        for (Program pr : project.getPrograms()) {
            if (pr instanceof JavaProgram) {
                List<String> str = Arrays.asList(pr.getLastCode().split("\n"));
                ArrayList<String> ret = new ArrayList<>();
                for (String s : str) {
                    if (s.contains("public") && s.contains("static") && s.contains("void") && s.contains("main")) {
                        ret.add(s);
                    }
                }
                if (!ret.isEmpty()) {
                    al.add(((JavaProgram) pr).getClassName());
                }
            }
        }

        return al;
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}
