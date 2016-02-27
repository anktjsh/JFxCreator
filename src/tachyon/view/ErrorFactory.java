/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.view;

import java.util.function.IntFunction;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.reactfx.value.Val;

/**
 *
 * @author Aniket
 */
public class ErrorFactory implements IntFunction<Node> {

    private final ObservableMap<Long, String> lines;

    public ErrorFactory(ObservableMap<Long, String> liner) {
        lines = liner;
    }

    @Override
    public Node apply(int lineNumber) {
        Polygon tri = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
        tri.setFill(Color.RED);
        MessageBooleanProperty visible = contains(lineNumber);
        if (visible.getValue()) {
            final Tooltip t = new Tooltip(visible.getMessage());
            Tooltip.install(tri, t);
        }
        tri.visibleProperty().bind(Val.flatMap(tri.sceneProperty(), scene -> {
            return scene != null ? visible : Val.constant(false);
        }));
        return tri;
    }

    private MessageBooleanProperty contains(int linenumber) {
        for (Long one : lines.keySet()) {
            if (one == linenumber) {
                return new MessageBooleanProperty(lines.get(one), true);
            }
        }
        return new MessageBooleanProperty("", false);
    }

    private class MessageBooleanProperty extends SimpleBooleanProperty {

        private final String mess;

        public MessageBooleanProperty(String message, boolean bl) {
            super(bl);
            mess = message;
        }

        public String getMessage() {
            return mess;
        }
    }

}
