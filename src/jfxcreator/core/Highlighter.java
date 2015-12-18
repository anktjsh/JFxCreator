/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import jfxcreator.view.Editor;
import org.fxmisc.richtext.CodeArea;

/**
 *
 * @author Aniket
 */
public class Highlighter {

    public static void highlight(CodeArea area, Editor ed) {
        if (ed.getScript().getType() == Program.JAVA) {
            JavaKeywordsAsync as = new JavaKeywordsAsync(area);
            as.apply();
        }
    }
}
