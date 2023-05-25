package courseObjects;

import org.opencv.core.Point;

public class Robot {
    private Point center;
    public Point rotationMarker;
    private double angle;
    public final double length = 16.0, width = 17.0, height = 18.0;

    public Robot(Point center, Point rotationMarker, double angle){
        this.center = center;
        this.rotationMarker = rotationMarker;
        this.angle = angle;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Point getCenter() {
        return center;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }
}
