package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import vision.BallPickupStrategy;

import java.util.ArrayDeque;
import java.util.Queue;

public class RoutingController {

    private final RobotController robotController;
    private final Course course;
    private final Queue<Routine> fullRoute = new ArrayDeque<>();
    private Routine currentRoute;


    public RoutingController(Course course) {
        this.course = course;
        this.robotController = new RobotController(course.getRobot());
    }

    /**
     * This method executes all planned routes in sequence
     */
    public void driveRoutes() throws InterruptedException {
        if (fullRoute.isEmpty()) return;

        while (!fullRoute.isEmpty()) {
            currentRoute = fullRoute.poll();
            if (currentRoute == null) return;

            currentRoute.run();
        }
    }

    private void addSubRoute(Routine routine) {
        fullRoute.add(routine);
    }

    // Clear planned route
    public void clearFullRoute() {
        fullRoute.clear();
    }

    // Stop ongoing route
    public void stopCurrentRoute() {
        robotController.stopMotors();
    }

    public Point projectPoint(@NotNull final Ball ball, final double distance) {
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
