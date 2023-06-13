package routing;

import courseObjects.Course;
import org.opencv.core.Point;

import java.util.ArrayDeque;
import java.util.Queue;

public class RoutingController {
    private final Course course;
    private final Queue<Route> fullRoute = new ArrayDeque<>();
    private Route nextRoute;
    private RobotController robotController;


    public RoutingController(Course course, String ip) {
        this.course = course;
        robotController = new RobotController(ip);
        nextRoute = new Route();
    }

    /**
     * This method executes all planned routes in sequence
     */
    public void driveRoutes () {
        if (fullRoute.isEmpty()) return;
        Route currentRoute = fullRoute.poll();
        if (currentRoute == null) return;

        /*TODO: execute currentRoute*/

        for (int i = 0; i < currentRoute.getDriveCommands().size(); i++) {
            currentRoute.getDriveCommands().get(i);
        }
    }

    /**
     * Plans next sequence of route from point to point
     */
    public void planRoute(Point from, Point to) {

        if (nextRoute.getTurns() > 0) {
            nextRoute.addDriveCommandToRoute(DriveCommand.ROTATE);
        }
        if (nextRoute.getEndingCommand() != null) {
            fullRoute.add(nextRoute);
            nextRoute.getDriveCommands().clear();
            return;
        }
        nextRoute.addDriveCommandToRoute(DriveCommand.DRIVE_STRAIGHT);
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
