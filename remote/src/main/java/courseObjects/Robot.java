package courseObjects;

import org.opencv.core.Point;

public class Robot {
    public Point center, rotationMarker;
    public double angle;
    public final double length = 16.0, width = 17.0, height = 18.0;

    public Robot(Point center, Point rotationMarker, double angle){
        this.center = center;
        this.rotationMarker = rotationMarker;
        this.angle = angle;
    }
}
