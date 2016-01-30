/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 * @author Aniket
 */
public class Template {

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
        }));
    }

    public static void save() {
        File f = new File(".cache" + File.separator + ".templates");
        if (!f.exists()) {
            f.mkdirs();
        }
        for (String s : getSavedTemplates().keySet()) {
            File save = new File(f.getAbsolutePath() + File.separator + s + ".txt");
            if (save.exists()) {
                save.delete();
            }
            try {
                Files.write(save.toPath(), getSavedTemplates().get(s));
            } catch (IOException ex) {
            }
        }
    }

    public static ObservableList<String> getAvailableTemplates() {
        ObservableList<String> list = FXCollections.observableArrayList("Java Class",
                "Java Main Class",
                "Java Interface",
                "JavaFx Main Class",
                "JavaFx Preloader",
                "Empty Java File",
                "Other File",
                "Empty FXML File",
                "Empty Text File",
                "Empty HTML File");
        list.addAll(getSavedTemplates().keySet());
        return list;
    }

    public static void addTemplate(String s, List<String> strng) {
        getSavedTemplates().put(s, strng);
    }

    private static ObservableMap<String, List<String>> savedTemplates;

    public static ObservableMap<String, List<String>> getSavedTemplates() {
        if (savedTemplates == null) {
            savedTemplates = FXCollections.observableMap(new TreeMap<String, List<String>>());
            loadSaved(savedTemplates);
        }
        return savedTemplates;
    }

    private static void loadSaved(ObservableMap<String, List<String>> map) {
        File f = new File(".cache" + File.separator + ".templates");
        if (f.exists()) {
            for (File k : f.listFiles()) {
                String s = k.getName().substring(0, k.getName().lastIndexOf("."));
                try {
                    map.put(s, Files.readAllLines(k.toPath()));
                } catch (IOException ex) {
                }
            }
        }
    }

    public static List<String> getTemplateCode(String desc, String pack, String clas) {
        switch (desc) {
            case "Java Class":
                return one(pack, clas);
            case "Java Main Class":
                return two(pack, clas);
            case "Java Interface":
                return three(pack, clas);
            case "JavaFx Main Class":
                return four(pack, clas);
            case "JavaFx Preloader":
                return five(pack, clas);
            case "Empty Java File":
                return six(pack, clas);
            case "Other File":
                return six(pack, clas);
            case "Empty FXML File":
                return eight();
            case "Empty Text File":
                return six(pack, clas);
            case "Empty HTML File":
                return six(pack, clas);
        }
        for (String s : getSavedTemplates().keySet()) {
            if (s.equals(desc)) {
                return getLines(getSavedTemplates().get(s), pack, clas);
            }
        }
        return new ArrayList<>();
    }

    private static List<String> getLines(List<String> al, String pack, String clas) {
        if (pack != null) {
            StringBuilder str = new StringBuilder();
            str.append(al.get(0));
            str.append("\npackage ").append(pack).append(";\n");
            str.append(al.get(1));
            str.append("\npublic class ").append(clas).append("\n");
            str.append(al.get(2));
            return Arrays.asList(str.toString().split("\n"));
        } else {
            StringBuilder str = new StringBuilder();
            str.append(al.get(0));
            str.append(al.get(1));
            str.append("\npublic class ").append(clas).append("\n");
            str.append(al.get(2));
            return Arrays.asList(str.toString().split("\n"));
        }
    }

    private static List<String> one(String pack, String clas) {
        if (pack != null) {
            String list = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> two(String pack, String clas) {
        if (pack != null) {
            String list = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "    public static void main (String args[]) {\n"
                    + "        System.out.println(\"Hello, World!\");\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public class " + clas + " {\n"
                    + "    \n"
                    + "    public static void main (String args[]) {\n"
                    + "        System.out.println(\"Hello, World!\");\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> three(String pack, String clas) {
        if (pack != null) {
            String list = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "public interface " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list = "\n"
                    + "public interface " + clas + " {\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> four(String pack, String clas) {
        if (pack != null) {
            String list
                    = "package " + pack + ";\n"
                    + "\n"
                    + "import javafx.application.Application;\n"
                    + "import javafx.event.ActionEvent;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.Button;\n"
                    + "import javafx.scene.layout.StackPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Application {\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage primaryStage) {\n"
                    + "        Button btn = new Button();\n"
                    + "        btn.setText(\"Say 'Hello World'\");\n"
                    + "        btn.setOnAction((ActionEvent event) -> {\n"
                    + "            System.out.println(\"Hello World!\");\n"
                    + "        });\n"
                    + "        \n"
                    + "        StackPane root = new StackPane();\n"
                    + "        root.getChildren().add(btn);\n"
                    + "        \n"
                    + "        Scene scene = new Scene(root, 300, 250);\n"
                    + "        \n"
                    + "        primaryStage.setTitle(\"Hello World!\");\n"
                    + "        primaryStage.setScene(scene);\n"
                    + "        primaryStage.show();\n"
                    + "    }\n"
                    + "\n"
                    + "    public static void main(String[] args) {\n"
                    + "        launch(args);\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list
                    = "\n"
                    + "import javafx.application.Application;\n"
                    + "import javafx.event.ActionEvent;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.Button;\n"
                    + "import javafx.scene.layout.StackPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Application {\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage primaryStage) {\n"
                    + "        Button btn = new Button();\n"
                    + "        btn.setText(\"Say 'Hello World'\");\n"
                    + "        btn.setOnAction((ActionEvent event) -> {\n"
                    + "            System.out.println(\"Hello World!\");\n"
                    + "        });\n"
                    + "        \n"
                    + "        StackPane root = new StackPane();\n"
                    + "        root.getChildren().add(btn);\n"
                    + "        \n"
                    + "        Scene scene = new Scene(root, 300, 250);\n"
                    + "        \n"
                    + "        primaryStage.setTitle(\"Hello World!\");\n"
                    + "        primaryStage.setScene(scene);\n"
                    + "        primaryStage.show();\n"
                    + "    }\n"
                    + "\n"
                    + "    public static void main(String[] args) {\n"
                    + "        launch(args);\n"
                    + "    }\n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }

    }

    private static List<String> five(String pack, String clas) {
        if (pack != null) {
            String list
                    = "\n"
                    + "package " + pack + ";\n"
                    + "\n"
                    + "import javafx.application.Preloader;\n"
                    + "import javafx.application.Preloader.ProgressNotification;\n"
                    + "import javafx.application.Preloader.StateChangeNotification;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.ProgressBar;\n"
                    + "import javafx.scene.layout.BorderPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Preloader {\n"
                    + "    \n"
                    + "    ProgressBar bar;\n"
                    + "    Stage stage;\n"
                    + "    \n"
                    + "    private Scene createPreloaderScene() {\n"
                    + "        bar = new ProgressBar();\n"
                    + "        BorderPane p = new BorderPane();\n"
                    + "        p.setCenter(bar);\n"
                    + "        return new Scene(p, 300, 150);        \n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage stage) throws Exception {\n"
                    + "        this.stage = stage;\n"
                    + "        stage.setScene(createPreloaderScene());        \n"
                    + "        stage.show();\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleStateChangeNotification(StateChangeNotification scn) {\n"
                    + "        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {\n"
                    + "            stage.hide();\n"
                    + "        }\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleProgressNotification(ProgressNotification pn) {\n"
                    + "        bar.setProgress(pn.getProgress());\n"
                    + "    }    \n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        } else {
            String list
                    = "\n"
                    + "import javafx.application.Preloader;\n"
                    + "import javafx.application.Preloader.ProgressNotification;\n"
                    + "import javafx.application.Preloader.StateChangeNotification;\n"
                    + "import javafx.scene.Scene;\n"
                    + "import javafx.scene.control.ProgressBar;\n"
                    + "import javafx.scene.layout.BorderPane;\n"
                    + "import javafx.stage.Stage;\n"
                    + "\n"
                    + "public class " + clas + " extends Preloader {\n"
                    + "    \n"
                    + "    ProgressBar bar;\n"
                    + "    Stage stage;\n"
                    + "    \n"
                    + "    private Scene createPreloaderScene() {\n"
                    + "        bar = new ProgressBar();\n"
                    + "        BorderPane p = new BorderPane();\n"
                    + "        p.setCenter(bar);\n"
                    + "        return new Scene(p, 300, 150);        \n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void start(Stage stage) throws Exception {\n"
                    + "        this.stage = stage;\n"
                    + "        stage.setScene(createPreloaderScene());        \n"
                    + "        stage.show();\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleStateChangeNotification(StateChangeNotification scn) {\n"
                    + "        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {\n"
                    + "            stage.hide();\n"
                    + "        }\n"
                    + "    }\n"
                    + "    \n"
                    + "    @Override\n"
                    + "    public void handleProgressNotification(ProgressNotification pn) {\n"
                    + "        bar.setProgress(pn.getProgress());\n"
                    + "    }    \n"
                    + "    \n"
                    + "}\n"
                    + "";
            return FXCollections.observableArrayList(list.split("\n"));
        }
    }

    private static List<String> six(String pack, String clas) {
        return new ArrayList<>();
    }

    private static List<String> eight() {
        String list = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<?import java.lang.*?>\n"
                + "<?import java.util.*?>\n"
                + "<?import javafx.scene.*?>\n"
                + "<?import javafx.scene.control.*?>\n"
                + "<?import javafx.scene.layout.*?>\n"
                + "\n"
                + "<AnchorPane id=\"AnchorPane\" prefHeight=\"400.0\" prefWidth=\"600.0\" xmlns:fx=\"http://javafx.com/fxml/1\">\n"
                + "    \n"
                + "</AnchorPane>\n"
                + "";
        return FXCollections.observableArrayList(list.split("\n"));
    }

}
