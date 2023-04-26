package courseObjects;

import org.opencv.core.Point;

import java.awt.*;

public class Ball {
    Point center;
    Color color;

    public Ball(Point center, Color color) {
        this.center = center;
        this.color = color;
    }
}
