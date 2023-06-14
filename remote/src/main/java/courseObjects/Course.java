package courseObjects;

import java.util.*;
import java.util.stream.Collectors;

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
    private final Queue<List<Ball>> ballWindow = new ArrayDeque<>();
    private final int BALL_WINDOW_SIZE = 10;
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
        return width;
    }

    public int getMaxNumberOfBalls() {
        return maxNumberOfBalls;
    }

    public void addBallListToWindow(List<Ball> balls) {
        // Add list of balls to window
        if (ballWindow.size() >= BALL_WINDOW_SIZE)
            ballWindow.poll();

        ballWindow.add(balls);

        // Find median list of balls, using size of list.
        List<List<Ball>> ballFrames = ballWindow.stream().sorted(Comparator.comparingInt(List::size)).toList();
        List<Ball> newBalls = ballFrames.get(ballFrames.size() / 2);

        // Transfer balls
        this.balls.clear();
        this.balls.addAll(newBalls);
    }
}
