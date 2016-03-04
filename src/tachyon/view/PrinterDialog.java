/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.Collation;
import javafx.print.PageRange;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
public class PrinterDialog {

    private final Stage stage;
    private final BorderPane main;
    private final VBox top, page, copies;
    private final Label status;
    private final Button cancel, ok;
    private final ComboBox<Printer> printers;

    private final PrinterJob currentJob;
    private final HBox center, bottom, pageSelection;
    private final RadioButton all, pages;
    private final TextField startPage, endPage;
    private final CheckBox collate;
    private final Spinner<Integer> spinner;

    private boolean success;

    public PrinterDialog(Window w, Node print) {
        stage = new Stage();
        stage.initOwner(w);
        stage.setTitle("Printer");
        stage.getIcons().add(Tachyon.icon);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        currentJob = PrinterJob.createPrinterJob();

        stage.setScene(new Scene(main = new BorderPane()));
        if (applyCss.get()) {
            stage.getScene().getStylesheets().add(css);
        }
        main.setPadding(new Insets(5, 10, 5, 10));
        printers = new ComboBox<>();
        for (Printer p : Printer.getAllPrinters()) {
            printers.getItems().add(p);
        }
        printers.setValue(Printer.getDefaultPrinter());
        main.setTop(top = new VBox(5));
        top.getChildren().addAll(
                new HBox(5,
                        new Label("Name : "),
                        printers),
                new HBox(5,
                        new Label("Status : "),
                        status = new Label("")));
        status.setText(currentJob.getJobStatus().name());
        currentJob.jobStatusProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                status.setText(newer.name());
            }
        });
        printers.setOnAction((e) -> {
            currentJob.setPrinter(printers.getValue());
        });
        main.setCenter(center = new HBox(25));
        center.getChildren().addAll(page = new VBox(5), copies = new VBox(5));
        center.setAlignment(Pos.CENTER);
        main.setBottom(bottom = new HBox(15));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.getChildren().addAll(cancel = new Button("Cancel"),
                ok = new Button("Confirm"));
        ok.setDefaultButton(true);
        cancel.setCancelButton(true);
        page.getChildren().addAll(new Label("Print Range"),
                all = new RadioButton("All"),
                pageSelection = new HBox(5,
                        pages = new RadioButton("Pages"),
                        new Label("from:"),
                        startPage = new TextField("1"),
                        new Label("to:"),
                        endPage = new TextField("1")));
        copies.getChildren().addAll(new Label("Copies"),
                new Label("Number of Copies"),
                spinner = new Spinner<>(1, Integer.MAX_VALUE, 1),
                collate = new CheckBox("Collate"));
        ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(all, pages);
        group.selectToggle(all);
        startPage.setDisable(true);
        endPage.setDisable(true);
        collate.setDisable(true);
        spinner.valueProperty().addListener((ob, older, newer) -> {
            if (newer > 1) {
                collate.setDisable(false);
            } else {
                collate.setDisable(true);
            }
        });
        startPage.setMaxWidth(50);
        endPage.setMaxWidth(50);
        pages.selectedProperty().addListener((ob, older, newer) -> {
            if (newer) {
                startPage.setDisable(false);
                endPage.setDisable(false);
            }
        });
        cancel.setOnAction((e) -> {
            success = false;
            stage.close();
        });
        stage.setOnCloseRequest((e) -> {
            success = false;
        });
        ok.setOnAction((e) -> {
            if (!collate.isDisabled()) {
                if (collate.isSelected()) {
                    currentJob.getJobSettings().setCollation(Collation.COLLATED);
                } else {
                    currentJob.getJobSettings().setCollation(Collation.UNCOLLATED);
                }
            }
            currentJob.getJobSettings().setCopies(spinner.getValue());
            if (group.getSelectedToggle() == pages) {
                try {
                    currentJob.getJobSettings().setPageRanges(new PageRange(Integer.parseInt(startPage.getText()),
                            Integer.parseInt(endPage.getText())));
                } catch (NumberFormatException rg) {
                }
            }
            boolean succe = currentJob.printPage(print);
            if (succe) {
                currentJob.endJob();
                success = true;
                stage.close();
            }
        });
    }

    public boolean showAndWait() {
        stage.showAndWait();
        return success;
    }

}
