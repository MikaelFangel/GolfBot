package courseObjects;

import java.util.*;
import org.opencv.core.Point;
import vision.Algorithms;

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
    private final Queue<List<Ball>> ballWindow = new ArrayDeque<>(); // To stabilize the List of Balls
    private final List<Ball> balls = Collections.synchronizedList(new ArrayList<>());
    private final Robot robot = new Robot();
    private final Cross cross = new Cross();

    private final double cameraHeight;

    private final int maxNumberOfBalls;

    public Course() {
        Properties configProp = configs.GlobalConfig.getConfigProperties();
        this.cameraHeight = Double.parseDouble(configProp.getProperty("camHeight"));

        // Measured from the innermost sides
        this.width = Double.parseDouble(configProp.getProperty("courseWidth"));
        this.height = Double.parseDouble(configProp.getProperty("courseHeight"));

        this.resolutionWidth = Integer.parseInt(configProp.getProperty("camResolutionWidth"));
        this.resolutionHeight = Integer.parseInt(configProp.getProperty("camResolutionHeight"));

        this.maxNumberOfBalls = Integer.parseInt(configProp.getProperty("maxBalls"));
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
        return this.width;
    }

    public int getMaxNumberOfBalls() {
        return this.maxNumberOfBalls;
    }

    /**
     * Add List<Ball> to Window. This window is used to average the list of ball to increase stability.
     * Only adds balls to window if balls are inside the course.
     * @param balls
     */
    public void addBallListToWindow(List<Ball> balls) {
        // Add list of balls to window
        int BALL_WINDOW_SIZE = 10;
        if (this.ballWindow.size() >= BALL_WINDOW_SIZE)
            this.ballWindow.poll();

        List<Ball> ballsInsideCourse = new ArrayList<>();
        for (Ball ball : balls) {
            boolean result = Algorithms.isOutsideCourse(ball.getCenter(), border.getCornersAsArray());
            if(result) {
                continue;
            }
            ballsInsideCourse.add(ball);
        }

        this.ballWindow.add(ballsInsideCourse);

        // Find median list of balls, using size of list.
        List<List<Ball>> ballFrames = this.ballWindow.stream().sorted(Comparator.comparingInt(List::size)).toList();
        List<Ball> newBalls = ballFrames.get(ballFrames.size() / 2);

        // Transfer balls
        this.balls.clear();
        this.balls.addAll(newBalls);
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
        this.cross.setEndPoints(newCross.getEndPoints());
        this.cross.setMiddle(newCross.getMiddle());
        this.cross.setMeasurePoint(newCross.getMeasurePoint());

        // Robot
        Robot newRobot = newCourse.getRobot();
        this.robot.setFrontAndCenter(newRobot.getCenter(), newRobot.getFront());

        // Balls
        List<Ball> newBalls = newCourse.getBalls();
        this.addBallListToWindow(newBalls);
    }
}
