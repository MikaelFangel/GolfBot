import courseObjects.Course;
import courseObjects.Cross;
import org.opencv.core.Point;
import vision.Algorithms;
import vision.math.Geometry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static java.awt.geom.Point2D.distance;
import static vision.math.Geometry.lineIsIntersectingCircle;

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

    // Clear planned routes
    public void clearFullRoute() {
        fullRoute.clear();
    }

    // Stop ongoing route
    public void stopCurrentRoute() {
        robotController.stopMotors();
    }
}
