package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import vision.BallPickupStrategy;

import java.sql.SQLOutput;
import java.util.ArrayDeque;
import java.util.Queue;

public class RoutingController {

    private final RobotController robotController;
    private final Course course;
    private final Queue<Routine> fullRoute = new ArrayDeque<>();
    private Routine currentRoute;


    public RoutingController(Course course) {
        this.course = course;
        this.robotController = new RobotController(course.getRobot());
    }

    /**
     * This method executes all planned routes in sequence
     */
    public void driveRoutes() {
        if (fullRoute.isEmpty()) return;

        while (!fullRoute.isEmpty()) {
            currentRoute = fullRoute.poll();
            if (currentRoute == null) return;

            currentRoute.run();
        }
    }

    /**
     * Add routine for how to pick-up a ball
     * @param ball the ball to be picked up
     */
    public void addRoutine(Ball ball) {
        Routine routine;
        switch (ball.getStrategy()) {
            case CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT ->
                    routine = new CollectCorner(course.getRobot().getCenter(), ball.getCenter(), ball, course.getCross(), this.robotController, this);
            case BORDER_BOTTOM, BORDER_LEFT, BORDER_RIGHT, BORDER_TOP, CROSS ->
                    routine = new CollectWallCross(course.getRobot().getCenter(), ball.getCenter(), ball, course.getCross(), this.robotController, this);
            case FREE ->
                    routine = new DriveAndCollect(course.getRobot().getCenter(), ball.getCenter(), ball, course.getCross(), this.robotController, this);
            default ->
                    routine = new DriveToPoint(course.getRobot().getCenter(), ball.getCenter(), ball, course.getCross(), this.robotController, this);
        }

        fullRoute.add(routine);
    }

    /**
     * Add a routine for driving to a specific destination
     * @param point the point to drive to
     */
    public void addRoutine(Point point) {
        fullRoute.add(new DriveToPoint(course.getRobot().getCenter(), point, null, course.getCross(), this.robotController, this));

    }

    /**
     * Clear planned route
     */
    public void clearFullRoute() {
        fullRoute.clear();
    }

    /**
     * Stop the current route and the robot
     */
    public void stopCurrentRoute() {
        robotController.stopMotors();
        robotController.stopCollectRelease();
    }

    /**
     * Project a point to a safe spot depending on the location of the ball.
     * @param ball ball to project
     * @param distance the distance to project from the safe margins
     * @return the projected point
     */
    public Point projectPoint(@NotNull final Ball ball, final double distance) {
        //TODO: get margin from config
        double borderDistance = 10 + distance;

        BallPickupStrategy strategy = ball.getStrategy();
        Point projectedPoint = null;
        switch (strategy) {
            case FREE -> projectedPoint = ball.getCenter();
            case BORDER_TOP -> projectedPoint = new Point(
                    ball.getCenter().x,
                    course.getBorder().getTopLeft().y + borderDistance
            );
            case BORDER_BOTTOM -> projectedPoint = new Point(
                    ball.getCenter().x,
                    course.getBorder().getBottomLeft().y - borderDistance
            );
            case BORDER_RIGHT -> projectedPoint = new Point(
                    course.getBorder().getTopRight().x - borderDistance,
                    ball.getCenter().y
            );
            case BORDER_LEFT -> projectedPoint = new Point(
                    course.getBorder().getTopLeft().x + borderDistance,
                    ball.getCenter().y
            );

            //TODO: make correction distance generic based on angel
            case CORNER_TOP_RIGHT -> projectedPoint = new Point(
                    ball.getCenter().x - borderDistance,
                    ball.getCenter().y - borderDistance
            );
            case CORNER_TOP_LEFT -> projectedPoint = new Point(
                    ball.getCenter().x + borderDistance,
                    ball.getCenter().y + borderDistance
            );
            case CORNER_BOTTOM_RIGHT -> projectedPoint = new Point(
                    ball.getCenter().x - borderDistance,
                    ball.getCenter().y + borderDistance
            );
            case CORNER_BOTTOM_LEFT -> projectedPoint = new Point(
                    ball.getCenter().x + borderDistance,
                    ball.getCenter().y - borderDistance
            );
            case CROSS -> {
                // TODO: Implement
                System.out.println("HIT UNIMPLEMENTED STRATEGY");
            }
            default -> projectedPoint = ball.getCenter();
        }

        return projectedPoint;
    }
}
