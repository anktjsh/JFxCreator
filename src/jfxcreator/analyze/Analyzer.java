/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.analyze;

import java.util.ArrayList;
import jfxcreator.analyze.Method.Parameter;

/**
 *
 * @author swatijoshi
 */
public class Analyzer {

    public static ArrayList<String> analyze(String name, String code, int caret, String currentWord) {
        Class clazz;
        if (name.contains(".")) {
            String a = name.substring(0, name.lastIndexOf('.'));
            String b = name.substring(name.lastIndexOf('.') + 1);
            clazz = Class.create(b, a, code);
        } else {
            clazz = Class.create(name, "", code);
        }
        ArrayList<String> ret = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            ret.add(m.getName());
        }
        Method m = clazz.identify(caret);
        if (m != null) {
            for (Parameter p : m.getParameters()) {
                ret.add(0, p.getName());
            }
        }
        ret.addAll(clazz.getImports());
        return ret;
    }
    
    public static class Option {
        private final String caption;
        private final String realText;
        public Option(String a, String b){
            caption =a;
            realText = b;
        }
    }
}
