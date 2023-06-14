package routing;

import courseObjects.Cross;
import math.Geometry;
import org.opencv.core.Point;
import vision.Algorithms;

import java.util.List;

public abstract class Routine {
    final Point start, dest;
    final Cross cross;
    final RobotController robotController;
    final RoutingController routingController;

    public Routine (Point start, Point dest, Cross cross, RobotController robotController, RoutingController routingController) {
        this.start = start;
        this.dest = dest;
        this.cross = cross;
        this.robotController = robotController;
        this.routingController = routingController;
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
                    cross.getLongestSide() / 2);
        } else
            return false;
    }
    public Point getIntermediatePointForObstructedRoute (Point point1, Point point2) {
        List<Point> accessiblePoints = PathController.findCommonPoints(
                point1,
                point2,
                Geometry.generateCircle(cross.getMiddle(),
                        cross.getLongestSide() + 20,
                        360),
                cross.getMiddle(),
                cross.getLongestSide()
        );

        return PathController.findShortestPath(point1, point2, accessiblePoints);
    }

    // TODO: Responsibility should be moved...
    public Double getDegreesToTurn(Point rotateTowards) {
        return Algorithms.findRobotShortestAngleToPoint(robotController.getRobot(), rotateTowards);
    }
}
