package routing;

import courseObjects.Ball;
import courseObjects.Course;
import courseObjects.Cross;
import math.Geometry;
import org.opencv.core.Point;
import vision.Algorithms;

import java.util.List;

public abstract class Routine {
    final Point start, dest;
    Point projectedPoint;
    final Cross cross;
    final Course course;
    private final int ROBOT_PADDING = 5;
    final Ball ballToCollect;
    final RobotController robotController;
    final RoutingController routingController;

    public Routine(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        this.start = start;
        this.dest = dest;
        this.cross = course.getCross();
        this.robotController = robotController;
        this.routingController = routingController;
        this.ballToCollect = ballToCollect;
        this.course = course;
    }

    public abstract void run();

    /**
     * Avoid the cross in the middle of the course and generate the projected point
     *
     * @param projectionDistance the distance to project the point if a projection is relevant
     */
    public void avoidObstacle(int projectionDistance) {
        if (this.ballToCollect != null)
            this.projectedPoint = routingController.projectPoint(ballToCollect, projectionDistance);
        else if (dest == routingController.getSmallGoalMiddlePoint()) {
            this.projectedPoint = routingController.getSmallGoalProjectedPoint();
        } else
            this.projectedPoint = dest;

        // Avoid obstacles
        if (isRouteObstructed(start, this.projectedPoint)) {
            Point intermediate = getIntermediatePointForObstructedRoute(start, this.projectedPoint);

            robotController.recalibrateGyro();
            robotController.rotate(getDegreesToTurn(intermediate));
            robotController.recalibrateGyro();
            robotController.drive(intermediate, false);
        }
    }

    // TODO: Responsibility should be moved...
    public Double getDegreesToTurn(Point rotateTowards) {
        return Algorithms.findRobotShortestAngleToPoint(robotController.getRobot(), rotateTowards);
    }

    private boolean isRouteObstructed(Point point1, Point point2) {
        // Check if the distance between the two points is greater than the distance to the cross
        if (Geometry.distanceBetweenTwoPoints(point1.x, point1.y, point2.x, point2.y) >
                Geometry.distanceBetweenTwoPoints(point1.x, point1.y, cross.getMiddle().x, cross.getMiddle().y)) {
            return Geometry.lineIsIntersectingCircle(
                    point1,
                    point2,
                    cross.getMiddle(),
                    cross.getLongestSide() / 2 + ROBOT_PADDING);
        } else
            return false;
    }

    private Point getIntermediatePointForObstructedRoute(Point point1, Point point2) {
        final int SAFE_CIRCLE_PADDING = 20;
        List<Point> accessiblePoints = PathController.findCommonPoints(
                point1,
                point2,
                Geometry.generateCircle(cross.getMiddle(),
                        cross.getLongestSide() + SAFE_CIRCLE_PADDING,
                        360),
                cross.getMiddle(),
                cross.getLongestSide() / 2 + ROBOT_PADDING,
                this.course
        );

        return PathController.findLongestPath(point1, point2, accessiblePoints);
    }
}
