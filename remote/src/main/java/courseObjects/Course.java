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

    private final Border border = new Border();
    private final List<Ball> balls = Collections.synchronizedList(new ArrayList<>());
    private final Robot robot = new Robot();
    private final Cross cross = new Cross();

    private final double cameraHeight;

    public Course() {
        this.cameraHeight = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("camHeight"));

        // Measured from the innermost sides
        this.width = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("courseWidth"));
        this.height = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("courseHeight"));

        this.resolutionWidth = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("camResolutionWidth"));
        this.resolutionHeight = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("camResolutionHeight"));
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

    /**
     * Replace the Course's objects variables with the new Course.
     * @param newCourse objects to be used for replacing.
     */
    public void replaceObjects(Course newCourse) {
        // Border
        Point[] newBorderCorners = newCourse.getBorder().getCornersAsArray();
        this.border.setTopLeft(newBorderCorners[0]);
        this.border.setTopRight(newBorderCorners[1]);
        this.border.setBottomLeft(newBorderCorners[2]);
        this.border.setBottomRight(newBorderCorners[3]);

        // Cross
        Cross newCross = newCourse.getCross();
        this.cross.setMiddle(newCross.getMiddle());
        this.cross.setMeasurePoint(newCross.getMeasurePoint());

        // Robot
        Robot newRobot = newCourse.getRobot();
        this.robot.setFrontAndCenter(newRobot.getCenter(), newRobot.getFront());

        // Balls TODO change when Magazine PR is merged
        List<Ball> newBalls = newCourse.getBalls();
        this.balls.clear();
        this.balls.addAll(newBalls);
    }
}
