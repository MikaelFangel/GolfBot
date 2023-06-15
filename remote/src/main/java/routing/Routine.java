package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import math.Geometry;
import org.opencv.core.Point;
import vision.Algorithms;

import java.util.List;

public abstract class Routine {
    final Point start, dest;
    Point projectedPoint;
    final Cross cross;
    private final int ROBOT_PADDING = 10;
    final Ball ballToCollect;
    final RobotController robotController;
    final RoutingController routingController;

    public Routine (Point start, Point dest,Ball ballToCollect, Cross cross, RobotController robotController, RoutingController routingController) {
        this.start = start;
        this.dest = dest;
        this.cross = cross;
        this.robotController = robotController;
        this.routingController = routingController;
        this.ballToCollect = ballToCollect;
    }
    public abstract void run();
    public boolean isRouteObstructed (Point point1, Point point2) {
        // Check if the distance between the two points is greater than the distance to the cross
        if(Geometry.distanceBetweenTwoPoints(point1.x, point1.y, point2.x, point2.y) >
                Geometry.distanceBetweenTwoPoints(point1.x, point1.y, cross.getMiddle().x, cross.getMiddle().y)) {
            return Geometry.lineIsIntersectingCircle(
                    point1,
                    point2,
                    cross.getMiddle(),
                    cross.getLongestSide() / 2 + ROBOT_PADDING);
        } else
            return false;
    }
    public Point getIntermediatePointForObstructedRoute (Point point1, Point point2) {
        final int SAFE_CIRCLE_PADDING = 20;
        List<Point> accessiblePoints = PathController.findCommonPoints(
                point1,
                point2,
                Geometry.generateCircle(cross.getMiddle(),
                        cross.getLongestSide() + SAFE_CIRCLE_PADDING,
                        360),
                cross.getMiddle(),
                cross.getLongestSide() + ROBOT_PADDING
        );

        return PathController.findShortestPath(point1, point2, accessiblePoints);
    }

    /**
     *
     * @param projectionDistance the distance to project the point if a projection is relevant
     */
    public void avoidObstacle(int projectionDistance) {
        if (this.ballToCollect != null)
            this.projectedPoint = routingController.projectPoint(ballToCollect, projectionDistance);
        else
            this.projectedPoint = dest;

        // Avoid obstacles
        if (isRouteObstructed(start, this.projectedPoint)) {
            Point intermediate = getIntermediatePointForObstructedRoute(start, this.projectedPoint);

            robotController.recalibrateGyro();
            robotController.rotate(getDegreesToTurn(intermediate));
            robotController.recalibrateGyro();
            robotController.drive(intermediate);
        }
    }

    // TODO: Responsibility should be moved...
    public Double getDegreesToTurn(Point rotateTowards) {
        return Algorithms.findRobotShortestAngleToPoint(robotController.getRobot(), rotateTowards);
    }
}
