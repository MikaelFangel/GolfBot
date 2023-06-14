package routing;

import courseObjects.Ball;
import courseObjects.Course;
import math.Geometry;
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

        while (!fullRoute.isEmpty()) {
            currentRoute = fullRoute.poll();
            if (currentRoute == null) return;

            for (int i = 0; i < currentRoute.getDriveCommands().size(); i++) {
                if (currentRoute.getEndingCommand() != null) {
                    handleEndCommand(currentRoute, currentRoute.getEndingCommand());
                }
                else handleCommand(currentRoute.getDriveCommands().get(i));
            }
        }

    }

    /**
     * planRoute plans next sequence of route from point to point,
     * considering obstruction of path, ball in corner and ball close to border,
     * which are handled with routines
     * TODO: get values from config e.g. circle radius
     *
     * @param from current point
     * @param to destination
     * @param endingCommand results in collection, release or routine for edge case
     * @return Route from one point to another
     */
    public Route planRoute(Point from, Point to, BallCommand endingCommand) {
        // If the current route has an endCommand, a routine is called
        if (currentRoute.getEndingCommand() != null) {
            handleEndCommand(currentRoute, endingCommand);
        }

        // Obstruction on path
        if (Geometry.lineIsIntersectingCircle(
                from,
                to,
                course.getCross().getMiddle(),
                course.getCross().getLongestSide()/2)
        ) {
            List<Point> accessiblePoints = PathController.findCommonPoints(
                    from,
                    to,
                    Geometry.generateCircle(course.getCross().getMiddle(),
                            course.getCross().getLongestSide() + 20,
                            360),
                    course.getCross().getMiddle(),
                    course.getCross().getLongestSide()
            );
            Point stopover = PathController.findShortestPath(from, to, accessiblePoints);

            Point dest = to;
            DRIVE_STRAIGHT.setNextPoint(stopover);
            addSubroute(nextRoute, endingCommand);
            DRIVE_STRAIGHT.setNextPoint(dest);
            addSubroute(nextRoute, endingCommand);
        }

        addSubroute(nextRoute, endingCommand);

        return currentRoute;
    }

    private Route addSubroute (Route route, BallCommand endingCommand) {
        route.addDriveCommandToRoute(ROTATE);

        if (route.getEndingCommand() != null) {
            fullRoute.add(route);
            route.getDriveCommands().clear();
        }
        route.addDriveCommandToRoute(DRIVE_STRAIGHT);

        return route;
    }

    //
    private void handleEndCommand(Route route, BallCommand endingCommand) {
        switch (endingCommand) {
            case COLLECT -> {}
            case RELEASE -> {}
            case RELEASE_ONE -> {}
            default -> {}
        }
    }

    public void handleCommand (DriveCommand driveCommand) {
        Point nextPoint = driveCommand.getNextPoint();
        switch (driveCommand) {
            case DRIVE_STRAIGHT -> {
                robotController.recalibrateGyro();
                robotController.drive(course.getRobot(), nextPoint);
            }
            case ROTATE -> {
                double angle = Algorithms.findShortestAngleToPoint(course.getRobot(), nextPoint);
                robotController.recalibrateGyro();
                robotController.rotate(-angle);
            }
            default -> {}
        }
    }

    // Clear planned route
    public void clearFullRoute() {
        fullRoute.clear();
    }

    // Stop ongoing route
    public void stopCurrentRoute() {
        robotController.stopMotors();
    }

    private Point projectPoint(@NotNull final Ball ball, final double distance) {
        //TODO: get margin from config
        int borderDistance = 20;

        BallPickupStrategy strategy = ball.getStrategy();
        Point projectedPoint = null;
        switch (strategy) {
            case FREE -> projectedPoint = ball.getCenter();
            case BORDER_TOP -> {
                projectedPoint = new Point(
                        ball.getCenter().x,
                        course.getBorder().getTopLeft().y + borderDistance
                );
            }
            case BORDER_BOTTOM -> {
                projectedPoint = new Point(
                        ball.getCenter().x,
                        course.getBorder().getBottomLeft().y - borderDistance
                );
            }
            case BORDER_RIGHT -> {
                projectedPoint = new Point(
                        course.getBorder().getTopRight().x - borderDistance,
                        ball.getCenter().y
                );
            }
            case BORDER_LEFT -> {
                projectedPoint = new Point(
                        course.getBorder().getTopLeft().x + borderDistance,
                        ball.getCenter().y
                );
            }

            //TODO: make correction distance generic based on angel
            case CORNER_TOP_RIGHT -> {
                projectedPoint = new Point(
                        ball.getCenter().x - borderDistance,
                        ball.getCenter().y - borderDistance
                );
            }
            case CORNER_TOP_LEFT -> {
                projectedPoint = new Point(
                        ball.getCenter().x + borderDistance,
                        ball.getCenter().y + borderDistance
                );
            }
            case CORNER_BOTTOM_RIGHT -> {
                projectedPoint = new Point(
                        ball.getCenter().x - borderDistance,
                        ball.getCenter().y + borderDistance
                );
            }
            case CORNER_BOTTOM_LEFT -> {
                projectedPoint = new Point(
                        ball.getCenter().x + borderDistance,
                        ball.getCenter().y - borderDistance
                );
            }
            case CROSS -> {
            }
            default -> projectedPoint = ball.getCenter();
        }

        return projectedPoint;
    }
}
