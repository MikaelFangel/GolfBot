import courseObjects.Course;
import courseObjects.Cross;
import org.opencv.core.Point;
import vision.Algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static java.awt.geom.Point2D.distance;

public class RoutingController {
    private final Course course;
    private final Queue<Route> fullRoute = new ArrayDeque<>();
    private Route currentRoute;
    private RobotController robotController;


    public RoutingController(Course course, String ip) {
        this.course = course;
        robotController = new RobotController(ip);
        currentRoute = new Route();
    }

    /**
     * This method executes all planned routes in sequence
     */
    public void driveRoutes () {
        if (fullRoute.isEmpty()) return;
        Route nextRoute = fullRoute.poll();
        if (nextRoute == null) return;

        /*TODO: execute nextRoute*/
        nextRoute.getDriveCommands();

    }

    // Plan next route from point to point
    public void planRoute(Point from, Point to) {

        //if (!fullRoute.isEmpty()) clearFullRoute();
        int turns = 0;

        if (lineIsIntersectingCircle(from, to, course.getCross().getMiddle(), course.getCross().getLongestSide()/2)) {
            /*TODO: handle reroute*/
            return;
        }
        currentRoute.setTurns(turns+1);
        currentRoute.addDriveCommandToRoute(DriveCommand.ROTATE);
        currentRoute.addDriveCommandToRoute(DriveCommand.DRIVE_STRAIGHT);
        currentRoute.setEndingCommand(BallCommand.COLLECT);

        fullRoute.add(currentRoute);

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
        robotController.stopMotors();
    }
}
