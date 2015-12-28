/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxcreator.view;

import java.util.function.IntFunction;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.reactfx.value.Val;

/**
 *
 * @author swatijoshi
 */
public class ArrowFactory implements IntFunction<Node> {

    private final ObservableList<Long> lines;

    public ArrowFactory(ObservableList<Long> liner) {
        lines = liner;
    }

    @Override
    public Node apply(int lineNumber) {
        Polygon triangle = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
        triangle.setFill(Color.RED);
        ObservableValue<Boolean> visible = contains(lineNumber);
        triangle.visibleProperty().bind(Val.flatMap(triangle.sceneProperty(), scene -> {
            return scene != null ? visible : Val.constant(false);
        }));
        return triangle;
    }

    private ObservableValue<Boolean> contains(int linenumber) {
        for (Long one : lines) {
            if (one == linenumber) {
                return new SimpleBooleanProperty(true);
            }
        }
        return new SimpleBooleanProperty(false);
    }

}
