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

    public static ArrayList<Option> analyze(String name, String code, int caret, String currentWord) {
        Class clazz;
        if (name.contains(".")) {
            String a = name.substring(0, name.lastIndexOf('.'));
            String b = name.substring(name.lastIndexOf('.') + 1);
            clazz = Class.create(b, a, code);
        } else {
            clazz = Class.create(name, "", code);
        }

        ArrayList<Option> ret = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            ret.add(new Option("Method : " + m.getName(), m.getName()));
        }
        Method m = clazz.identify(caret);
        if (m != null) {
            for (Parameter p : m.getParameters()) {
                ret.add(0, new Option("Variable : " + p.getName(), p.getName()));
            }
        }
        for (String s : clazz.getImports()) {
            ret.add(new Option("Class : " + s, s));
        }
        return ret;
    }

    public static class Option {

        private final String caption;
        private final String realText;

        public Option(String a, String b) {
            caption = a;
            realText = b;
        }

        /**
         * @return the caption
         */
        public String getCaption() {
            return caption;
        }

        /**
         * @return the realText
         */
        public String getRealText() {
            return realText;
        }

        @Override
        public String toString() {
            return getCaption();
        }
    }
}
