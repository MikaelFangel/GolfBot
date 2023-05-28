package courseObjects;

import org.opencv.core.Point;

public class Robot {
    private Point center;
    private Point rotationMarker;
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

    public void setRotationMarker(Point rotationMarker) {
        this.rotationMarker = rotationMarker;
    }

    public Point getRotationMarker() {
        return rotationMarker;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }
}
