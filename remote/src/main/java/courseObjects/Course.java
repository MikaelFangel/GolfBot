package courseObjects;

import org.opencv.core.Point;

import java.util.Collections;
import java.util.List;

/**
 * The course consists of (1) the dimensions of the course, (2) coordinates of the corners in centimeters,
 * (3) a list containing the balls located on the course to be picked up by the robot, and (4) the robot object
 */
public class Course {
    private final double length, width; // Measured from the innermost sides
    private Point topLeft, topRight, bottomLeft, bottomRight; // The corners of the border.
    private List<Ball> balls;
    private Robot robot;

    public Course() {
        this.length = 167.0;
        this.width = 122.0;
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

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public String printCorners() {
        return "Border{" +
                "topLeft=" + topLeft +
                ", topRight=" + topRight +
                ", bottomLeft=" + bottomLeft +
                ", bottomRight=" + bottomRight +
                '}';
    }
}
