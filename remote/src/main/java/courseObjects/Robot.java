package courseObjects;

import org.opencv.core.Point;
import vision.math.Geometry;

public class Robot {
    private Point center, front;
    private double angle;
    public final double length = 16.0, width = 17.0, height = 20.0; // CM

    public Robot(Point center, Point front){
        this.center = center;
        this.front = front;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }
    
    public Robot(){};

    public synchronized Point getCenter() {
        return center;
    }

    public synchronized void setFrontAndCenter(Point center, Point front) {
        this.front = front;
        this.center = center;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }

    public synchronized Point getFront() {
        return front;
    }

    public synchronized void setAngle(double angle) {
        this.angle = angle;
    }

    public synchronized double getAngle() {
        return angle;
    }
}
