package courseObjects;

import org.opencv.core.Point;

import java.awt.*;

public class Ball {
    private Point center;
    private Color color;

    public Ball(Point center, Color color) {
        this.center = center;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

}
