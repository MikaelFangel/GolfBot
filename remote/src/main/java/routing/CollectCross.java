package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;

public class CollectCross extends Routine {

    public CollectCross(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    @Override
    public void run() {
        avoidObstacle(15);

        Point crossBefore = course.getCross().getMiddle().clone();

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false, 80, 3);

        Point crossAfter = course.getCross().getMiddle().clone();
        if (Math.abs(crossBefore.x - crossAfter.x) > 2 || Math.abs(crossBefore.y - crossAfter.y) > 2)
            return;

        // Correct the robot for collection
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.collectRelease(true);
        super.robotController.drive(dest, true, 40, 4);
        super.robotController.stopCollectRelease();

        super.robotController.reverse();
    }
}
