/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.analyze;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class Class {

    private final String name;
    private final String packageName;
    private final ObservableList<String> imports;
    private final ObservableList<Method> methods;
    private final String code;

    private Class(String name, String packageName, String text) {
        this.name = name;
        this.packageName = packageName;
//        System.out.println(name);
//        System.out.println(packageName);
        code = text;
        imports = FXCollections.observableArrayList();
        methods = FXCollections.observableArrayList();
        importer();
        analyze();
    }

    public static Class create(String cName, String pName, String text) {
        return new Class(cName, pName, text);
    }

    public String getName() {
        return name;
    }

    private void importer() {
        int first = code.indexOf("import");
        int last = code.lastIndexOf("import");
        if (first != -1 && last != -1) {
            int fin = code.substring(last).indexOf(';') + last;
            String imp = code.substring(first, fin + 1);
            String spl[] = imp.split(";");
            for (String s : spl) {
                String temp = s.trim();
                imports.add(getClass(temp.substring(temp.indexOf("import") + 6).trim()));
            }
        }
    }

    private String getClass(String s) {
        if (s.contains(".")) {
            return s.substring(s.lastIndexOf('.') + 1);
        } else {
            return s;
        }
    }

    private void analyze() {
        int first = code.indexOf('{');
        int last = code.lastIndexOf('}');
        if (first != last && first != -1 && last != -1) {
            String temp = code.substring(first + 1, last);
            while (temp.contains("{") && temp.contains("}")) {
                int one = temp.indexOf('{');
                int two = temp.indexOf('}');
                int loc = 0;
                if (temp.substring(one + 1, two).contains("{")) {
                    loc = getLocation(temp.substring(two + 1), getOpenCount(temp.substring(one + 1, two)), two);
                } else {
                    loc = -1;
                }
                method(code, first + 1 + one + 1, first + 1 + two + 1 + loc);
                temp = temp.substring(two + 1 + loc + 1 + 1);
                first = first + two + 1 + loc + 1 + 1;
            }
        }
    }

    private void method(String s, int start, int end) {
        int index1 = s.substring(0, start + 1).lastIndexOf('{');
        int now = -1;
        for (int x = index1 - 1; x >= 0; x--) {
            if (s.charAt(x) == '{' || s.charAt(x) == '}' || s.charAt(x) == ';' || s.charAt(x) == '/') {
                now = x;
                break;
            }
        }
//        System.out.println(s.substring(now+1, end)+'}');
        methods.add(Method.create(this, s.substring(now + 1, end) + '}', start, end));
    }

    public Method identify(int caret) {
        for (Method m : methods) {
            if (caret >= m.getStart() && caret < m.getEnd()) {
                return m;
            }
        }
        return null;
    }

    public ObservableList<String> getImports() {
        return imports;
    }

    public ObservableList<Method> getMethods() {
        return methods;
    }

    private int getLocation(String text, int close, int two) {
        int loc = 0;
        if (close == 0) {
            return two;
        }
        while (text.contains("}")) {
            int one = text.indexOf('}');
            loc += one;
            close--;
            if (close == 0) {
                break;
            }
            text = text.substring(one + 1);

        }
        return loc;
    }

    private int getOpenCount(String text) {
        int count = 0;
        while (text.contains("{")) {
            text = text.substring(text.indexOf('{') + 1);
            count++;
        }
        return count;
    }

}
