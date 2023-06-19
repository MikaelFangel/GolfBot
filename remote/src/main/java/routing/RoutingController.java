package routing;

import courseObjects.Ball;
import courseObjects.Course;
import courseObjects.Cross;
import math.Geometry;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import vision.BallPickupStrategy;

import java.util.ArrayDeque;
import java.util.Deque;

public class RoutingController {

    private final RobotController robotController;
    private final Course course;
    private final Deque<Routine> fullRoute = new ArrayDeque<>();
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
            currentRoute = fullRoute.pop();
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
            case CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT, CROSS ->
                    routine = new CollectCorner(course.getRobot().getCenter(), ball.getCenter(), ball, this.robotController, this, course);
            case BORDER_BOTTOM, BORDER_LEFT, BORDER_RIGHT, BORDER_TOP ->
                    routine = new CollectWallCross(course.getRobot().getCenter(), ball.getCenter(), ball, this.robotController, this, course);
            case FREE ->
                    routine = new DriveAndCollect(course.getRobot().getCenter(), ball.getCenter(), ball, this.robotController, this, course);
            default ->
                    routine = new DriveToPoint(course.getRobot().getCenter(), ball.getCenter(), ball, this.robotController, this, course);
        }

        fullRoute.add(routine);
    }

    /**
     * Add a routine for driving to a specific destination
     * @param point the point to drive to
     */
    public void addRoutine(Point point, boolean deliverBalls) {
        if(deliverBalls)
            fullRoute.add(new DeliverBallsToGoal(course.getRobot().getCenter(), point, null, this.robotController, this, course));
        else
            fullRoute.add(new DriveToPoint(course.getRobot().getCenter(), point, null, this.robotController, this, course));
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
        double crossProjectionMargin = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("crossProjectionMargin"));
        double borderProjectionMargin = Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("borderProjectionMargin"));

        double borderDistance = borderProjectionMargin + distance;
        double crossDistance = crossProjectionMargin + course.getCross().getLongestSide() / 2 + distance;

        BallPickupStrategy strategy = ball.getStrategy();
        Point projectedPoint;

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
                Point ballCenter = ball.getCenter();
                Cross cross = course.getCross();
                Point crossCenter = cross.getMiddle(), crossMeasurePoint = cross.getMeasurePoint();

                // Prepare angles and distances
                double angleToBall = Geometry.angleBetweenTwoPoints(crossCenter.x, crossCenter.y, ballCenter.x, ballCenter.y);
                double angleToMeasurePoint = Geometry.angleBetweenTwoPoints(crossCenter.x, crossCenter.y, crossMeasurePoint.x, crossMeasurePoint.y);
                double distanceToBall = Geometry.distanceBetweenTwoPoints(crossCenter.x, crossCenter.y, ballCenter.x, ballCenter.y);

                // If ball is NOT between two "legs"
                if (distanceToBall > cross.getLongestSide() / 2) {
                    projectedPoint = new Point(
                            crossCenter.x + Math.cos(angleToBall) * crossDistance,
                            crossCenter.y + Math.sin(angleToBall) * crossDistance
                    );
                } else { //  if ball IS between two "legs"

                    // Calculate number of times to rotate 90 degrees.
                    double diffAngle = angleToBall - angleToMeasurePoint;
                    double numRotations = (int) (diffAngle / 90); // To remove decimals.
                    int direction = (diffAngle >= 0) ? 1 : -1;

                    double projectionAngle = angleToMeasurePoint + (direction * 45) + (90 * numRotations);
                    // Convert to radians
                    projectionAngle = projectionAngle * Math.PI / 180;

                    projectedPoint = new Point(
                            crossCenter.x + Math.cos(projectionAngle) * crossDistance,
                            crossCenter.y + Math.sin(projectionAngle) * crossDistance
                    );
                }

            }
            default -> projectedPoint = ball.getCenter();
        }

        return projectedPoint;
    }

    public Point getSmallGoalMiddlePoint() {
        return course.getBorder().getSmallGoalMiddlePoint();
    }

    public Point getSmallGoalProjectedPoint() {
        return course.getBorder().getSmallGoalDestPoint();
    }
}
