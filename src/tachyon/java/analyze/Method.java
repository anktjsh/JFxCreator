/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.java.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class Method {

    private final ObservableList<String> modifiers;
    private final ObservableList<Parameter> parameters;
    private final Class container;
    private final String name;
    private final String code;
    private final int start, end;

    private Method(Class c, String n, String text, List<String> mod, List<Parameter> param, int start, int end) {
        container = c;
        name = n;
        code = text;
        this.start = start;
        this.end = end;
        modifiers = FXCollections.observableArrayList(mod);
        parameters = FXCollections.observableArrayList(param);
    }

    public static Method create(Class c, String text, int start, int end) {
        int open = text.indexOf('{');
        String parenSearch = text.substring(0, open);
        int op = parenSearch.lastIndexOf('(');
        int cl = parenSearch.lastIndexOf(')');
        String par = parenSearch.substring(op + 1, cl);
        String para[] = par.split(",");
        for (int x = 0; x < para.length; x++) {
            para[x] = para[x].trim();
        }
        ArrayList<Parameter> parameters = new ArrayList<>();
        for (String s : para) {
            String[] split = s.split(" ");
            if (split.length == 2) {
                parameters.add(new Parameter(split[0].trim(), split[1].trim()));
            }
        }
        String mod = parenSearch.substring(0, op);
//        System.out.println(mod);
        String[] split = mod.split(" ");
        for (int x = 0; x < split.length; x++) {
            split[x] = split[x].trim();
        }
        ArrayList<String> al = new ArrayList<>();
        for (int x = 0; x < split.length - 1; x++) {
            al.add(split[x]);
        }

        return new Method(c, split[split.length - 1], text, al, parameters, start, end);
    }

    public ObservableList<Parameter> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public boolean isConstructor() {
        return (container.getName().equals(getName()));
    }

    public boolean doesOverride() {
        return modifiers.contains("Override");
    }

    public boolean hasAnnotations() {
        for (String s : modifiers) {
            if (s.contains("@")) {
                return true;
            }
        }
        return false;
    }

    public boolean isFinal() {
        return modifiers.contains("final");
    }

    public boolean isPublic() {
        return modifiers.contains("public");
    }

    public boolean isPrivate() {
        return modifiers.contains("private");
    }

    public boolean isStatic() {
        return modifiers.contains("static");
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    public static class Parameter {

        private final String type;
        private final String name;

        public Parameter(String one, String tw) {
            type = one;
            name = tw;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
    }

}
