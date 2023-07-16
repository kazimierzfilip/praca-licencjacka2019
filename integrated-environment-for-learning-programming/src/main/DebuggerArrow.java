package main;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class DebuggerArrow extends Polygon {
    public DebuggerArrow() {
        this.getPoints().addAll(0.0, 5.0,
                0.0, -5.0,
                10.0, 0.0);
        this.setFill(Color.GREEN);
    }
}
