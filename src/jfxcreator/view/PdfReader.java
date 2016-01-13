/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import javafx.application.Platform;
import jfxcreator.core.Program;
import jfxcreator.core.Project;

/**
 *
 * @author Aniket
 */
public class PdfReader extends EnvironmentTab {

    public PdfReader(Program scr, Project pro) {
        super(scr, pro);
        BaseViewer f;
        setContent(f = new BaseViewer());
        selectedProperty().addListener((ob, older, newer) -> {
            if (newer) {
                if (f.getPDFfilename() == null) {
                    (new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        Platform.runLater(() -> {
                            f.loadPDF(scr.getFile().toFile());
                        });
                    })).start();
                }
            }
        });

    }

}
