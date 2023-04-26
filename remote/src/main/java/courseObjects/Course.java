package courseObjects;

import org.opencv.core.Point;

import java.util.List;

public class Course {
    public final double length = 167.0, width = 122.0;
    public Point topLeft, topRight, bottomLeft, bottomRight;

    private List<Ball> balls;
    public Robot robot;

    public List<Ball> getBalls() {
        return balls;
    }

    public void setBalls(List<Ball> balls) {
        this.balls = balls;
    }
}
