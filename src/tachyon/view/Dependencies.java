/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

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
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tachyon.Tachyon;
import static tachyon.Tachyon.applyCss;
import static tachyon.Tachyon.css;

/**
 *
 * @author Aniket
 */
public class Dependencies {

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
        stage.getIcons().add(Tachyon.icon);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnCloseRequest((e) -> {
            e.consume();
            Optional<ButtonType> show = Writer.showAlert(Alert.AlertType.CONFIRMATION, stage, "Exit", "Are you sure you want to exit?", "");
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
            String s = "http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html";
            Tachyon.host.showDocument(s);
            Writer.showAlert(Alert.AlertType.INFORMATION, stage, "", "Download the latest JDK and click Refresh at the top", "");
        });
        refresh = new Button("Refresh");
        refresh.setOnAction((e) -> {
            options.getItems().clear();
            options.getItems().addAll(getAvailableOptions());
        });
        box.getChildren().addAll(new Label("Select the appropriate JDK Version"), refresh, options, field, link, confirm = new Button("Confirm"));
        options.valueProperty().addListener((ob, older, newer) -> {
            field.setText(options.getValue());
        });

        directory = new TextField();
        directory.setPromptText("Projects Home");
        String home = System.getProperty("user.home");
        directory.setText(home + File.separator + "Documents"
                + File.separator + "TachyonProjects");
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
            Writer.showAlert(Alert.AlertType.INFORMATION, stage, "Configuration Set",
                    "Settings Applied", "");
            stage.close();
        });
        stage.setScene(new Scene(box));
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
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
            Files.write(Paths.get(".cache" + File.separator + "preferences03.txt"),
                    FXCollections.observableArrayList(localVersionProperty.get(), alert + "", workplace_location));
            Files.write(Paths.get(".cache" + File.separator + "design03.txt"),
                    FXCollections.observableArrayList(Tachyon.applyCss.get() + ""));
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
        System.out.println(p.toAbsolutePath().toString());
        String OS = System.getProperty("os.name").toLowerCase();
        Path a = Paths.get(localVersionProperty.get() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        Path b = Paths.get(localVersionProperty.get() + File.separator + "javafxpackager" + (OS.contains("win") ? ".exe" : ""));
        System.out.println(Files.exists(a));
        System.out.println(Files.exists(b));
        if (!Files.exists(p)) {
            Writer.showAlert(AlertType.ERROR, w, "Java Platform",
                    "Previous JDK Files no longer exist",
                    "Click on Settings and Select a new JDK Version or some features of Tachyon may not work properly");
        } else if (!(Files.exists(a) || Files.exists(b))) {
            Writer.showAlert(AlertType.ERROR, w, "Java Platform",
                    "JDK Version is outdated, some features of Tachyon may not work as expected",
                    "Please download a more recent version of the Java JDK");
        }
    }

    public static void platform(Window w) {
        Stage s = new Stage();
        s.setTitle("Java Platform");
        s.setResizable(false);
        s.getIcons().add(Tachyon.icon);
        s.setWidth(400);
        s.setHeight(400);
        s.initOwner(w);
        s.initModality(Modality.APPLICATION_MODAL);
        VBox box;
        s.setScene(new Scene(box = new VBox(10)));
        if (applyCss.get()) {
            s.getScene().getStylesheets().add(css);
        }
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
        ChoiceBox<String> choice;
        TextField field;
        Button cancel, confirm;
        HBox h;
        Button refresh = new Button("Refresh");
        Hyperlink link = new Hyperlink("Click to Download Latest Version of JDK");
        link.setOnAction((e) -> {
            String sa = "http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html";
            Tachyon.host.showDocument(sa);
            Writer.showAlert(Alert.AlertType.INFORMATION, s, "", "Download the latest JDK and click Refresh at the top", "");
        });
        box.getChildren().addAll(new Label("Select Java Platform"),
                refresh,
                new Label("Current Platform is : " + Dependencies.localVersionProperty.get()),
                choice = new ChoiceBox<>(),
                field = new TextField(),
                h = new HBox(10,
                        cancel = new Button("Cancel"),
                        confirm = new Button("Confirm")),
                link);
        refresh.setOnAction((e) -> {
            choice.getItems().clear();
            choice.getItems().addAll(getAvailableOptions());
        });
        choice.getItems().addAll(getAvailableOptions());
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
            Dependencies.localVersionProperty.set(sa);
            Writer.showAlert(Alert.AlertType.INFORMATION, s, "Configuration Set",
                    "Settings Applied", "");
            String OS = System.getProperty("os.name").toLowerCase();
            Path a = Paths.get(localVersionProperty.get() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
            Path b = Paths.get(localVersionProperty.get() + File.separator + "javafxpackager" + (OS.contains("win") ? ".exe" : ""));
            if (!(Files.exists(a) || Files.exists(b))) {
                Writer.showAlert(AlertType.ERROR, w, "Java Platform",
                        "JDK Version is low, some features of Tachyon may not work as expected",
                        "Please download a more recent version of the Java JDK");
            }
            s.close();
        });
        s.showAndWait();
    }

    public static void workplace(Window w) {
        Stage s = new Stage();
        s.setTitle("Workplace Location");
        s.setResizable(false);
        s.getIcons().add(Tachyon.icon);
        s.setWidth(400);
        s.setHeight(400);
        s.initOwner(w);
        s.initModality(Modality.APPLICATION_MODAL);
        VBox box;
        s.setScene(new Scene(box = new VBox(10)));
        if (applyCss.get()) {
            s.getScene().getStylesheets().add(css);
        }
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
                new Label("Project Workplace Location"),
                dir,
                choose,
                defau = new Button("Select Default Directory")
        ));
        defau.setOnAction((e) -> {
            String home = System.getProperty("user.home");
            home = home + File.separator + "Documents"
                    + File.separator + "TachyonProjects";
            Optional<ButtonType> show = Writer.showAlert(AlertType.CONFIRMATION, s, "Settings", "Project Workplace Location", "Select " + home + "\nAs your project directory?");
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
            Writer.showAlert(Alert.AlertType.INFORMATION, s, "Settings", "Project Workplace Location", "Configurations Saved");
            s.close();
        });
        s.showAndWait();
    }

}
