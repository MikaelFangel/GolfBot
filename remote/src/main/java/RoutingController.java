import courseObjects.Course;
import courseObjects.Cross;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class RoutingController {
    private final Course course;
    private final List<Route> fullRoute = new ArrayList<>();
    private Route currentRoute;
    private RobotController robotController;

    // Drive next planned route

    public RoutingController(Course course) {
        this.course = course;
    }
    //
    public void driveRoutes () {
        if (fullRoute.isEmpty()) return;
        Route nextRoute = fullRoute.iterator().next();
        if (nextRoute == null) return;
        fullRoute.remove(nextRoute);
        /*TODO: execute nextRoute*/

    }

    // Plan next route
    public void planRoute(Point curr, Point next) {
        if (!fullRoute.isEmpty()) clearFullRoute();
        Route nextRoute = fullRoute.iterator().next();
        /*TODO: algorithm for planning next ball*/




        driveRoutes();
    }

    /**
     * This method checks whether a given line calculated from two points is intersecting a circle.
     * @param from the starting point of the line
     * @param to the ending point of the line
     * @param circleCenter the center point of the circle (eg. course.getCross().getMiddle())
     * @param circleRadius the radius of the circle to check if intersecting  (eg. course.getLongestSide() / 2)
     * @return true if the line is intersecting with the circle
     */
    public boolean lineIsIntersectingCircle(Point from, Point to, Point circleCenter, double circleRadius) {
        // Calculate the line between two points
        double a = (from.y - to.y) / (from.x - to.x);
        double b = from.y - a * from.x;

        // Calculate the distance from the center of the circle
        double dist = Math.abs(a*circleCenter.x + b - circleCenter.y) / Math.sqrt(Math.pow(a, 2) + 1);
        return dist < circleRadius;
    }

    // Clear planned routes
    public void clearFullRoute() {
        fullRoute.clear();
    }

    // Stop ongoing route
    public void stopCurrentRoute() {

    }
}
