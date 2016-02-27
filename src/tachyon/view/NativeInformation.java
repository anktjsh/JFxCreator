/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 *
 * @author Aniket
 */
public class NativeInformation extends BorderPane {

    private final VBox box;
    private final Hyperlink link;

    public NativeInformation() {
        box = new VBox(5);
        box.getChildren().addAll(
                new HBox(5,
                        new Text("Download ispack-5.5.3.exe or newer from "),
                        link = new Hyperlink("Inno Setup Downloads page")),
                new Text("Double-click the file to launch the installer"),
                new Text("Accept the Inno Setup license agreement and click Next"),
                new Text("Follow the instructions in the install wizard for installing Inno Setup"),
                new Text("Right click on your computer in Windows Explorer\n"
                        + "Select Properties, then Advanced System Settings"),
                new Text("Select the Advanced Tab and double click the Environment Variables button"),
                new Text("In the System Variables pane, double-click the Path variable"),
                new Text("In the Edit System Variable dialog box, \nadd a semicolon followed by a new path to the \nVariable value field (for example, C:\\Program Files (x86)\\Inno Setup 5"),
                new Text("After completing all of these steps, close Tachyon and restart it"));

        link.setOnAction((e) -> {
            tachyon.Tachyon.host.showDocument("http://www.jrsoftware.org/isdl.php");
        });
        setCenter(box);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER);
    }
}
