package courseObjects;

import org.opencv.core.Point;
import math.Geometry;

public class Robot {
    private Point center, front;
    private double angle;
    // length is distance between markers, width is distance between wheels, height is height of the biggest marker (CM)
    //public final double length = 16.0, width = 17.0, height = 21.5;
    public final double length, width, height;

    public Robot(Point center, Point front) {
        this(); // Call default constructor

        this.center = center;
        this.front = front;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }

    public Robot() {
        // Measurements are in CM
        this.length = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("robotLength"));
        this.width = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("robotWidth"));
        this.height = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("robotHeight"));
    }

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
