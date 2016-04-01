/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import tachyon.core.Program;
import tachyon.core.Project;

/**
 *
 * @author Aniket
 */
public class PdfReader extends EnvironmentTab {

    public PdfReader(Program scr, Project pro) {
        super(scr, pro);
        BaseViewer f;
        setContent(f = new BaseViewer());
        f.loadPDF(scr.getFile().toFile());
    }

}
