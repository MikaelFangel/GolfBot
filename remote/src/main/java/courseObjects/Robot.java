package courseObjects;

import org.opencv.core.Point;
import vision.math.Geometry;

public class Robot {
    private Point center, front;
    private double angle;
    // length is distance between markers, width is distance between wheels, height is height of the biggest marker (CM)
    public final double length = 16.0, width = 17.0, height = 21.5;

    public Robot(Point center, Point front){
        this.center = center;
        this.front = front;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }

    public void setPosition(Point center, Point front) {
        this.center = center;
        this.front = front;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Point getCenter() {
        return center;
    }

    public void setFront(Point front) {
        this.front = front;
    }

    public Point getFront() {
        return front;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }
}
