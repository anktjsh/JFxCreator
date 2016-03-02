/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.util.function.IntFunction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.reactfx.value.Val;

/**
 *
 * @author Aniket
 */
public class BreakPointFactory implements IntFunction<Node> {

    private final ObservableList<Long> lines;

    public BreakPointFactory(ObservableList<Long> liner) {
        lines = liner;
    }

    @Override
    public Node apply(int lineNumber) {
        Circle tri = new Circle(5, Color.DARKRED);
        BooleanProperty visible = contains(lineNumber);
        tri.visibleProperty().bind(Val.flatMap(tri.sceneProperty(), scene -> {
            return scene != null ? visible : Val.constant(false);
        }));
        return tri;
    }

    private BooleanProperty contains(int linenumber) {
        for (Long one : lines) {
            if (one.intValue() - 1 == linenumber) {
                return new SimpleBooleanProperty(true);
            }
        }
        return new SimpleBooleanProperty(false);
    }

}
