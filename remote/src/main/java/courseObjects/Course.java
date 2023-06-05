package courseObjects;

import org.opencv.core.Point;

import java.util.Collections;
import java.util.List;

public class Course {
    public final double length = 167.0, width = 122.0;
    private Point cameraPosition;

    private Point topLeft, topRight, bottomLeft, bottomRight;
    private List<Ball> balls;
    private Robot robot;
    private double cameraHeight;

    public Course(double cameraHeight){
        this.cameraHeight = cameraHeight;
    }


    // Getters and setters
    public synchronized Point getTopLeft() {
        return topLeft;
    }

    public synchronized void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public synchronized Point getTopRight() {
        return topRight;
    }

    public synchronized void setTopRight(Point topRight) {
        this.topRight = topRight;
    }

    public synchronized Point getBottomLeft() {
        return bottomLeft;
    }

    public synchronized void setBottomLeft(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public synchronized Point getBottomRight() {
        return bottomRight;
    }

    public synchronized void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public synchronized List<Ball> getBalls() {
        return balls;
    }

    public synchronized void setBalls(List<Ball> balls) {
        this.balls = Collections.synchronizedList(balls);
    }

    public synchronized Robot getRobot() {
        return robot;
    }

    public synchronized void setRobot(Robot robot) {
        this.robot = robot;
    }

    public double getCameraHeight() {
        return cameraHeight;
    }

    public Point getCameraPosition() {
        return cameraPosition;
    }

    public void setCameraPosition(Point cameraPosition) {
        this.cameraPosition = cameraPosition;
    }
}
