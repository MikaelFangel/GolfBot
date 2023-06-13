package routing;

import courseObjects.Course;
import org.opencv.core.Point;

import java.util.ArrayDeque;
import java.util.Queue;

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

    /**
     * Plans next sequence of route from point to point
     */
    public void planRoute(Point from, Point to) {

        if (currentRoute.getTurns() > 0) {
            currentRoute.addDriveCommandToRoute(DriveCommand.ROTATE);
        }
        if (currentRoute.getEndingCommand() != null) {
            fullRoute.add(currentRoute);
            currentRoute.getDriveCommands().clear();
            return;
        }
        currentRoute.addDriveCommandToRoute(DriveCommand.DRIVE_STRAIGHT);
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
