/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import java.util.List;
import javafx.collections.FXCollections;

/**
 *
 * @author Aniket
 */
public class Examples {

    private final List<String> all = FXCollections.observableArrayList(
            "WebBrowser", "MediaPlayer");

    private Examples() {

    }

    public List<String> getAllExamples() {
        return all;
    }

    public int getIndex(String s) {
        for (int x = 0; x < all.size(); x++) {
            if (all.get(x).equals(s)) {
                return x;
            }
        }
        return -1;
    }

    public String getCode(int n) {
        if (n == 0) {
            return webBrowserLauncherCode;
        }
        return "";
    }

    private static Examples examples;

    public Examples getExamples() {
        if (examples == null) {
            examples = new Examples();
        }
        return examples;
    }

    private final String webBrowserLauncherCode = "public class Launcher extends Application {\n"
            + "\n"
            + "    private static Browser web;\n"
            + "\n"
            + "    @Override\n"
            + "    public void start(Stage stage) throws Exception {\n"
            + "        stage.setScene(new Scene(web = new Browser()));\n"
            + "        stage.setTitle(\"JavaFx Web Browser\");\n"
            + "        web.getEngine().load(\"https://google.com\");\n"
            + "        stage.show();\n"
            + "    }\n"
            + "\n"
            + "    public static void begin(Stage stage) {\n"
            + "        stage.setScene(new Scene(web = new Browser()));\n"
            + "        stage.setTitle(\"JavaFx Web Browser\");\n"
            + "        web.getEngine().load(\"https://google.com\");\n"
            + "        stage.show();\n"
            + "    }\n"
            + "\n"
            + "    private static class Browser extends BorderPane {\n"
            + "\n"
            + "        private final WebView web;\n"
            + "        private final ToolBar box;\n"
            + "        private final Button back, forward, refresh, go;\n"
            + "        private final TextField name;\n"
            + "        private final ProgressIndicator indic;\n"
            + "\n"
            + "        public Browser() {\n"
            + "            web = new WebView();\n"
            + "            setCenter(web);\n"
            + "            setTop(new BorderPane(box = new ToolBar()));\n"
            + "            box.setPadding(new Insets(5, 10, 5, 10));\n"
            + "\n"
            + "            box.getItems().addAll(back = new Button(\"<\"),\n"
            + "                    forward = new Button(\">\"),\n"
            + "                    new Separator(),\n"
            + "                    indic = new ProgressIndicator(),\n"
            + "                    refresh = new Button(\"Refresh\"),\n"
            + "                    new Separator(),\n"
            + "                    name = new TextField(),\n"
            + "                    go = new Button(\"->\")\n"
            + "            );\n"
            + "            name.setMaxWidth(400);\n"
            + "            name.setMinWidth(400);\n"
            + "            web.getEngine().locationProperty().addListener((ob, older, newer) -> {\n"
            + "                name.setText(newer);\n"
            + "            });\n"
            + "            name.setOnAction((e) -> {\n"
            + "                String s = name.getText();\n"
            + "                if (!s.startsWith(\"https://\")) {\n"
            + "                    name.setText(\"https://\" + s);\n"
            + "                }\n"
            + "                getEngine().load(name.getText());\n"
            + "            });\n"
            + "            name.focusedProperty().addListener((ob, older, newer) -> {\n"
            + "                if (newer) {\n"
            + "                    name.selectAll();\n"
            + "                }\n"
            + "            });\n"
            + "            go.setOnAction(name.getOnAction());\n"
            + "            name.setContextMenu(new ContextMenu());\n"
            + "            name.getContextMenu().getItems().addAll(new MenuItem(\"Copy link address\"), new MenuItem(\"Select All\"));\n"
            + "            name.getContextMenu().getItems().get(0).setOnAction((e) -> {\n"
            + "                Clipboard cl = Clipboard.getSystemClipboard();\n"
            + "                ClipboardContent cc = new ClipboardContent();\n"
            + "                cc.putString(name.getText());\n"
            + "                cc.putUrl(name.getText());\n"
            + "                cl.setContent(cc);\n"
            + "            });\n"
            + "            name.getContextMenu().getItems().get(1).setOnAction((e) -> {\n"
            + "                name.selectAll();\n"
            + "            });\n"
            + "            back.setOnAction((e) -> {\n"
            + "                try {\n"
            + "                    web.getEngine().getHistory().go(-1);\n"
            + "                } catch (IndexOutOfBoundsException ae) {\n"
            + "                }\n"
            + "            });\n"
            + "            forward.setOnAction((e) -> {\n"
            + "                try {\n"
            + "                    web.getEngine().getHistory().go(1);\n"
            + "                } catch (IndexOutOfBoundsException ae) {\n"
            + "                }\n"
            + "            });\n"
            + "            refresh.setOnAction((e) -> {\n"
            + "                web.getEngine().reload();\n"
            + "            });\n"
            + "\n"
            + "            web.getEngine().setJavaScriptEnabled(true);\n"
            + "            web.getEngine().setUserAgent(\"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36\");\n"
            + "            web.getEngine().getLoadWorker().progressProperty().addListener((ob, older, newer) -> {\n"
            + "                indic.setProgress(newer.doubleValue());\n"
            + "            });\n"
            + "\n"
            + "            web.getEngine().setPromptHandler((param) -> {\n"
            + "                TextInputDialog dialog = new TextInputDialog(param.getDefaultValue());\n"
            + "                dialog.setTitle(\"Prompt\");\n"
            + "                dialog.initOwner(getScene().getWindow());\n"
            + "                dialog.setHeaderText(param.getMessage());\n"
            + "                Optional<String> result = dialog.showAndWait();\n"
            + "                if (result.isPresent()) {\n"
            + "                    return result.get();\n"
            + "                }\n"
            + "                return \"\";\n"
            + "            });\n"
            + "\n"
            + "            web.getEngine().setCreatePopupHandler((PopupFeatures param) -> {\n"
            + "                Stage st = new Stage();\n"
            + "                WebView wb = new WebView();\n"
            + "                st.setScene(new Scene(wb));\n"
            + "                st.show();\n"
            + "                return wb.getEngine();\n"
            + "            });\n"
            + "            web.getEngine().setOnAlert((e) -> {\n"
            + "                Alert al = new Alert(Alert.AlertType.INFORMATION);\n"
            + "                al.initOwner(getScene().getWindow());\n"
            + "                al.setTitle(\"Alert\");\n"
            + "                al.setHeaderText(e.getData());\n"
            + "                al.showAndWait();\n"
            + "            });\n"
            + "            web.getEngine().setOnError((e) -> {\n"
            + "                Alert al = new Alert(Alert.AlertType.ERROR);\n"
            + "                al.initOwner(getScene().getWindow());\n"
            + "                al.setTitle(\"Error\");\n"
            + "                al.setHeaderText(e.getMessage());\n"
            + "\n"
            + "                StringWriter sw = new StringWriter();\n"
            + "                PrintWriter pw = new PrintWriter(sw);\n"
            + "                e.getException().printStackTrace(pw);\n"
            + "                String exceptionText = sw.toString();\n"
            + "\n"
            + "                Label label = new Label(\"The exception stacktrace was:\");\n"
            + "\n"
            + "                TextArea textArea = new TextArea(exceptionText);\n"
            + "                textArea.setEditable(false);\n"
            + "                textArea.setWrapText(true);\n"
            + "\n"
            + "                textArea.setMaxWidth(Double.MAX_VALUE);\n"
            + "                textArea.setMaxHeight(Double.MAX_VALUE);\n"
            + "                GridPane.setVgrow(textArea, Priority.ALWAYS);\n"
            + "                GridPane.setHgrow(textArea, Priority.ALWAYS);\n"
            + "\n"
            + "                GridPane expContent = new GridPane();\n"
            + "                expContent.setMaxWidth(Double.MAX_VALUE);\n"
            + "                expContent.add(label, 0, 0);\n"
            + "                expContent.add(textArea, 0, 1);\n"
            + "\n"
            + "                al.getDialogPane().setExpandableContent(expContent);\n"
            + "                al.showAndWait();\n"
            + "            });\n"
            + "            web.getEngine().setConfirmHandler((String msg) -> {\n"
            + "                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);\n"
            + "                alert.setTitle(\"Confirmation\");\n"
            + "                alert.setHeaderText(msg);\n"
            + "                alert.initOwner(getScene().getWindow());\n"
            + "\n"
            + "                Optional<ButtonType> result = alert.showAndWait();\n"
            + "                return result.get() == ButtonType.OK;\n"
            + "            });\n"
            + "        }\n"
            + "\n"
            + "        public final WebEngine getEngine() {\n"
            + "            return web.getEngine();\n"
            + "        }\n"
            + "    }\n"
            + "\n"
            + "    public static void main(String[] args) {\n"
            + "        launch(args);\n"
            + "    }\n"
            + "\n"
            + "}\n"
            + "";

}
