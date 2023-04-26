package courseObjects;

import org.opencv.core.Point;

public class Robot {
    public Point center;
    public double angle;
    public final double length = 19.0, width = 17.0, height = 18.0;

    public Robot(Point center, double angle){
        this.center = center;
        this.angle = angle;
    }
}
