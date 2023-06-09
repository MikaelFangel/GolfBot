package courseObjects;

import org.opencv.core.Point;

import java.util.Collections;
import java.util.List;

/**
 * The course consists of (1) the dimensions of the course, (2) coordinates of the corners in centimeters,
 * (3) a list containing the balls located on the course to be picked up by the robot, and (4) the robot object
 */
public class Course {
    private final double width = 169.0, height = 123.7; // Measured from the innermost sides
    private Point cameraPosition;

    private final int resolutionWidth = 1024;
    private final int ResolutionHeight = 768;

    private Border border;
    private List<Ball> balls;
    private Robot robot;
    private final double cameraHeight;

    public Course(double cameraHeight){
        this.cameraHeight = cameraHeight;
    }

    // Getters and setters
    public Border getBorder() {
        return border;
    }

    public void setBorder(Border border) {
        this.border = border;
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

    public int getResolutionHeight() {
        return ResolutionHeight;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}
