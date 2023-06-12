package courseObjects;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The course consists of (1) the dimensions of the course, (2) coordinates of the corners in centimeters,
 * (3) a list containing the balls located on the course to be picked up by the robot, and (4) the robot object
 * <br/><br/>
 * NB! All measurements are given in centimetres
 */
public class Course {
    private final double width, height;
    private final int resolutionWidth;
    private final int resolutionHeight;

    private Point cameraPosition;

    private final Border border = new Border();
    private final List<Ball> balls = Collections.synchronizedList(new ArrayList<>());
    private final Robot robot = new Robot();
    private final Cross cross = new Cross();

    private final double cameraHeight;

    public Course(double cameraHeight) {
        this.cameraHeight = cameraHeight;

        // Measured from the innermost sides
        this.width = 169.0;
        this.height = 123.7;

        this.resolutionWidth = 1024;
        this.resolutionHeight = 768;
    }

    // Getters and setters
    public Border getBorder() {
        return this.border;
    }

    public List<Ball> getBalls() {
        return this.balls;
    }

    public Robot getRobot() {
        return this.robot;
    }

    public Cross getCross() {
        return this.cross;
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
