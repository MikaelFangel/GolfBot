package routing;

import org.opencv.core.Point;

import java.util.List;

import static math.Geometry.distanceBetweenTwoPoints;
import static math.Geometry.lineIsIntersectingCircle;

public class PathController {

    /**
     * given the position of 2 objects and a list of possible points that both can see, it figure how many of these points
     * can be reached without hitting the given obstacle.
     *
     * @param object1 first objects position
     * @param object2 second objects position
     * @param possibleSharedPoints All the points that need to be checked, if they are reachable
     * @param circle The position of the obstacle
     * @param circleRadius the size of the obstacle
     * @return All the Points from possibleSharedPoints, that are reachable without colliding with the obstacle
     */
    public static List<Point> findCommonPoints(
            Point object1,
            Point object2,
            List<Point> possibleSharedPoints,
            Point circle,
            double circleRadius
    ){
        return possibleSharedPoints.stream()
                .parallel()
                .filter(point -> !lineIsIntersectingCircle(object1, point, circle, circleRadius)) //removes all points that can't reach object 1
                .filter(point -> !lineIsIntersectingCircle(object2, point, circle, circleRadius)) //removes all points that can't reach object 2
                .toList(); //convert it into a list
    }

    /**
     * Given 2 objects position, and a list of points that both can reach in a straight line,
     * it finds the point for which the sum of movement required is the lowest.
     *
     * @param point1 first objects position
     * @param point2 second objects position
     * @param sharedPoints a list of points that can be reach in a straight line from both points
     * @return A single point from the sharedPoints list, that have the shortest distance.
     */
    public static Point findShortestPath(
            Point point1,
            Point point2,
            List<Point> sharedPoints
    ) {
        if (sharedPoints.isEmpty()) return null;
        return sharedPoints.stream()
                .parallel()
                .min((p1,p2) -> { //returns the value based on the sorting that has the lowest value
                    double point1Distance1 = distanceBetweenTwoPoints(p1.x, p1.y, point1.x, point1.y);
                    double point1Distance2 = distanceBetweenTwoPoints(p1.x, p1.y, point2.x, point2.y);
                    double point2Distance1 = distanceBetweenTwoPoints(p2.x, p2.y, point1.x, point1.y);
                    double point2Distance2 = distanceBetweenTwoPoints(p2.x, p2.y, point2.x, point2.y);
                    return (int) ((point1Distance1 + point1Distance2) - (point2Distance1 + point2Distance2));
                }).orElse(null);

    }
}
