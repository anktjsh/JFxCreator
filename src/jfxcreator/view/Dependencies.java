/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import jfxcreator.JFxCreator;

/**
 *
 * @author Aniket
 */
public class Dependencies {

    public static final String CURRENT_VERSION = "1.8.0_45";

    private final Stage stage;
    private final VBox box;
    private final TextField field;
    private final ComboBox<String> options;
    private final Hyperlink link;
    private final Button confirm, refresh;

    private final TextField directory;
    private final Button chooser;

    public Dependencies(Window w) {
        stage = new Stage();
        stage.setTitle("Dependencies");
        stage.setResizable(false);
        stage.setTitle("Select Preferences");
        stage.initOwner(w);
        stage.getIcons().add(JFxCreator.icon);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnCloseRequest((e) -> {
            e.consume();
            Alert al = new Alert(Alert.AlertType.CONFIRMATION);
            al.setTitle("Exit");
            al.setHeaderText("Are you sure you want to exit?");
            al.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            al.initOwner(stage);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            Optional<ButtonType> show = al.showAndWait();
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    Platform.exit();
                    System.exit(0);
                }
            }
        });
        box = new VBox(10);
        field = new TextField();
        field.setPromptText("JDK File Path");
        field.setEditable(false);
        options = new ComboBox<>();
        options.getItems().addAll(getAvailableOptions());
        link = new Hyperlink("Click to Download Latest Version of JDK");
        link.setOnAction((e) -> {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setHeaderText("Download the latest JDK and click Refresh at the top");
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            al.showAndWait();
            String s = "http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html";
            JFxCreator.host.showDocument(s);
        });
        refresh = new Button("Refresh");
        refresh.setOnAction((e) -> {
            options.getItems().setAll(getAvailableOptions());
        });
        box.getChildren().addAll(new Label("Select the appropriate JDK Version"), refresh, options, field, link, confirm = new Button("Confirm"));
        options.valueProperty().addListener((ob, older, newer) -> {
            field.setText(options.getValue());
        });

        directory = new TextField();
        directory.setPromptText("Projects Home");
        String home = System.getProperty("user.home");
        directory.setText(home + File.separator + "Documents"
                + File.separator + "JFxCreatorProjects");
        chooser = new Button("Choose a Directory");
        chooser.setOnAction((e) -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Choose a location");
            File show = dc.showDialog(stage);
            if (show != null) {
                directory.setText(show.getAbsolutePath());
            }
        });
        box.getChildren().addAll(box.getChildren().indexOf(confirm), FXCollections.observableArrayList(new Label("Choose a Directory for your Projects"), directory, chooser));

        confirm.setOnAction((e) -> {
            String s = options.getValue();
            if (getVersion(s).compareTo(CURRENT_VERSION) < 0) {
                Alert al = new Alert(Alert.AlertType.WARNING);
                al.setHeaderText("This version of Java is outdated.\nIf you continue to use it, some features of JFxCreator may not work.\nDo you want to continue?");
                al.getButtonTypes().add(ButtonType.CANCEL);
                al.initOwner(stage);
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("Configuration Set");
            al.setHeaderText("Settings Applied");
            al.initOwner(stage);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            al.showAndWait();

            stage.close();

        });
        stage.setScene(new Scene(box));
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
    }

    private static String getVersion(String s) {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            return getWindowsVersion(s);
        } else {
            return getMacVersion(s);
        }
    }

    private static String getWindowsVersion(String s) {
        String sh = s.substring(s.indexOf("Java") + 8, s.lastIndexOf(File.separator));
        return sh;
    }

    private static String getMacVersion(String s) {
        String sh = s.substring(s.indexOf("JavaVirtualMachines") + 24, s.lastIndexOf(".jdk"));
        return sh;
    }

    private static List<String> getAvailableOptions() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.contains("windows")) {
            return windowList();
        } else {
            return macList();
        }
    }

    public String showAndWait() {
        stage.showAndWait();
        return field.getText() + "," + directory.getText();
    }

    private static List<String> windowList() {
        File f = new File("C:" + File.separator + "Program Files" + File.separator + "Java" + File.separator);
        ArrayList<String> al = new ArrayList<>();
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(fl.getAbsolutePath() + File.separator + "bin");
                }
            }
        }
        f = new File("C:" + File.separator + "Program Files (x86)" + File.separator + "Java" + File.separator);
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(fl.getAbsolutePath() + File.separator + "bin");
                }
            }
        }
        return al;
    }

    private static List<String> macList() {
        File f = new File("/Library/Java/JavaVirtualMachines/");
        ArrayList<String> al = new ArrayList<>();
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(f.getAbsolutePath() + File.separator + fl.getName() + "/Contents/Home/bin");
                }
            }
        }
        return al;
    }

    private static boolean alert;
    public static ObjectProperty<String> localVersionProperty = new SimpleObjectProperty<>("");
    public static String workplace_location;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
        }));
    }

    public static void save() {
        Path p = Paths.get(".cache");
        try {
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
            Files.write(Paths.get(".cache" + File.separator + "preferences03.txt"), FXCollections.observableArrayList(localVersionProperty.get(), alert + "", workplace_location));
        } catch (IOException ex) {
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Object hidden = Files.getAttribute(p, "dos:hidden", LinkOption.NOFOLLOW_LINKS);
                if (hidden != null) {
                    if (hidden instanceof Boolean) {
                        Boolean bool = (Boolean) hidden;
                        if (!bool) {
                            Files.setAttribute(p, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
                        }
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public static void load(Window w) {
        File f = new File(".cache" + File.separator + "preferences03.txt");
        if (f.exists()) {
            try {
                Scanner in = new Scanner(f);
                String s = in.nextLine();
                boolean b = Boolean.parseBoolean(in.nextLine());
                localVersionProperty.set(s);
                alert = b;
                workplace_location = in.nextLine();
            } catch (Exception ex) {
                alert = true;
            }
        } else {
            alert = true;
        }
        if (alert) {
            String s = new Dependencies(w).showAndWait();
            String[] spl = s.split(",");
            localVersionProperty.set(spl[0]);
            alert = false;
            workplace_location = spl[1];
        }
        Path p = Paths.get(localVersionProperty.get());
        if (!Files.exists(p)) {
            Alert al = new Alert(AlertType.ERROR);
            al.setTitle("Java Platform");
            al.initOwner(w);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            al.setHeaderText("Previous JDK Files no longer exist");
            al.setContentText("Click on Settings and Select a new JDK Version or some features of JFxCreator may not work properly");
            al.showAndWait();
        }
        System.setProperty("java.home", Dependencies.localVersionProperty.get().replace("bin", "jre"));
    }

    public static void platform(Window w) {
        Stage s = new Stage();
        s.setTitle("Java Platform");
        s.setResizable(false);
        s.getIcons().add(JFxCreator.icon);
        s.setWidth(400);
        s.setHeight(400);
        s.initOwner(w);
        s.initModality(Modality.APPLICATION_MODAL);
        VBox box;
        s.setScene(new Scene(box = new VBox(10)));
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        ChoiceBox<String> choice;
        TextField field;
        Button cancel, confirm;
        HBox h;
        box.getChildren().addAll(new Label("Select Java Platform"),
                new Label("Current Platform is : " + Dependencies.localVersionProperty.get()),
                choice = new ChoiceBox<>(),
                field = new TextField(),
                h = new HBox(10,
                        cancel = new Button("Cancel"),
                        confirm = new Button("Confirm")));
        choice.getItems().setAll(getAvailableOptions());
        field.setPromptText("Java Platform");
        h.setAlignment(Pos.CENTER);
        field.setText(Dependencies.localVersionProperty.get());
        field.setEditable(false);
        choice.valueProperty().addListener((ob, older, newer) -> {
            field.setText(choice.getValue());
        });
        cancel.setOnAction((e) -> {
            s.close();
        });
        confirm.setOnAction((e) -> {
            String sa = field.getText();

            if (getVersion(sa).compareTo(CURRENT_VERSION) < 0) {
                Alert al = new Alert(Alert.AlertType.WARNING);
                al.setHeaderText("This version of Java is outdated.\nIf you continue to use it, some features of JFxCreator may not work.\nDo you want to continue?\n"
                        + "(Optimal Version is jdk1.8.0_45)");
                al.getButtonTypes().add(ButtonType.CANCEL);
                al.initOwner(s);
                ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK) {
                        Dependencies.localVersionProperty.set(sa);
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } else {
                Dependencies.localVersionProperty.set(sa);
            }
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("Configuration Set");
            al.setHeaderText("Settings Applied");
            al.initOwner(s);
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            al.showAndWait();

            s.close();
        });
        s.showAndWait();
    }

    public static void workplace(Window w) {
        Stage s = new Stage();
        s.setTitle("Workplace Location");
        s.setResizable(false);
        s.getIcons().add(JFxCreator.icon);
        s.setWidth(400);
        s.setHeight(400);
        s.initOwner(w);
        s.initModality(Modality.APPLICATION_MODAL);
        VBox box;
        s.setScene(new Scene(box = new VBox(10)));
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        HBox bottom;
        box.getChildren().add(bottom = new HBox(10));
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(5, 10, 5, 10));
        Button cancel, save, defau;
        bottom.getChildren().addAll(cancel = new Button("Cancel"),
                save = new Button("Save"));
        TextField dir = new TextField(workplace_location);
        dir.setPromptText("Projects Home");
        dir.setEditable(false);
        Button choose = new Button("Select Directory");
        box.getChildren().addAll(0, FXCollections.observableArrayList(
                new Text("Project Workplace Location"),
                dir,
                choose,
                defau = new Button("Select Default Directory")
        ));
        defau.setOnAction((e) -> {
            Alert al = new Alert(AlertType.CONFIRMATION);
            al.initOwner(s);
            al.setTitle("Settings");
            al.setHeaderText("Project Workplace Location");

            String home = System.getProperty("user.home");
            home = home + File.separator + "Documents"
                    + File.separator + "JFxCreatorProjects";
            al.setContentText("Select " + home + "\nAs your project directory?");
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            Optional<ButtonType> show = al.showAndWait();
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    dir.setText(home);
                    save.fire();
                }
            }
        });
        choose.setOnAction((e) -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Choose a location");
            File show = dc.showDialog(s);
            if (show != null) {
                dir.setText(show.getAbsolutePath());
            }
        });
        cancel.setOnAction((e) -> {
            s.close();
        });
        save.setOnAction((E) -> {
            workplace_location = dir.getText();
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("Settings");
            al.initOwner(s);
            al.setHeaderText("Project Workplace Location");
            al.setContentText("Configurations Saved");
            ((Stage) al.getDialogPane().getScene().getWindow()).getIcons().add(JFxCreator.icon);
            al.showAndWait();
            s.close();
        });
        s.showAndWait();
    }

}
