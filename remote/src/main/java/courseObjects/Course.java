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
        return this.border;
    }

    public void setBorder(Border border) {
        this.border = border;
    }

    public synchronized List<Ball> getBalls() {
        return this.balls;
    }

    public synchronized void setBalls(List<Ball> balls) {
        this.balls = Collections.synchronizedList(balls);
    }

    public synchronized Robot getRobot() {
        return this.robot;
    }

    public synchronized void setRobot(Robot robot) {
        this.robot = robot;
    }

    public double getCameraHeight() {
        return this.cameraHeight;
    }

    public Point getCameraPosition() {
        return this.cameraPosition;
    }

    public void setCameraPosition(Point cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public int getResolutionHeight() {
        return this.resolutionHeight;
    }

    public int getResolutionWidth() {
        return this.resolutionWidth;
    }

    public double getHeight() {
        return this.height;
    }

    public double getWidth() {
        return width;
    }
}
