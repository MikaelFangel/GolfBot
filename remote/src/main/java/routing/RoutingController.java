package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import vision.BallPickupStrategy;
import vision.Algorithms;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static routing.DriveCommand.DRIVE_STRAIGHT;
import static routing.DriveCommand.ROTATE;

public class RoutingController {
    private final Course course;
    private final Queue<Route> fullRoute = new ArrayDeque<>();
    private Route nextRoute;
    private Route currentRoute;
    private final RobotController robotController;


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
        currentRoute = fullRoute.poll();
        if (currentRoute == null) return;

        for (int i = 0; i < currentRoute.getDriveCommands().size(); i++) {
            handleCommand(currentRoute.getDriveCommands().get(i), DRIVE_STRAIGHT.getNextPoint());
        }
    }

    /**
     * Plans next sequence of route from point to point
     */
    public void planRoute(Point from, Point to) {

        nextRoute.addDriveCommandToRoute(ROTATE);

        if (nextRoute.getEndingCommand() != null) {
            fullRoute.add(nextRoute);
            nextRoute.getDriveCommands().clear();
            return;
        }
        nextRoute.addDriveCommandToRoute(DRIVE_STRAIGHT);
    }

    public void handleCommand (DriveCommand driveCommand, Point nextPoint) {
        switch (driveCommand) {
            case DRIVE_STRAIGHT :
                robotController.recalibrateGyro();
                robotController.driveWGyro(course);
                break;
            case ROTATE :
                double angle = Algorithms.findShortestAngleToPoint(course.getRobot(), nextPoint);
                robotController.recalibrateGyro();
                robotController.rotateWGyro(-angle);
                break;
            default : break;
        }
    }

    // Clear planned routes
    public void clearFullRoute() {
        fullRoute.clear();
    }

    // Stop ongoing route
    public void stopCurrentRoute() {
        robotController.stopMotors();
    }

    private Point projectPoint(@NotNull final Ball ball, final double distance) {
        BallPickupStrategy strategy = ball.getStrategy();
        Point projectedPoint;
        switch (strategy) {
            case FREE -> projectedPoint = ball.getCenter();
            default -> projectedPoint = ball.getCenter();
        }

        return projectedPoint;
    }
}
