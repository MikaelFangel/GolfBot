package courseObjects;

import org.opencv.core.Point;

import java.awt.Color;

public class Ball {
    private Point center;
    private BallColor color;

    public Ball(Point center, BallColor color) {
        this.center = center;
        this.color = color;
    }

    public BallColor getColor() {
        return color;
    }

    public void setColor(BallColor color) {
        this.color = color;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

}
