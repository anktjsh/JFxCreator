/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.core;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class Console {

    private final ObservableList<String> list;

    public Console() {
        list = FXCollections.observableArrayList();
    }

    public ObservableList<String> getList() {
        return list;
    }

    public void log(String st) {
        list.add(st);
    }

    public void merge(Console col) {
        col.getList().addAll(0, getList());
        getList().addListener(new ListChangeListener<String>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                c.next();
                if (c.wasAdded()) {
                    col.getList().addAll(c.getAddedSubList());
                }
            }

        });
    }
}
