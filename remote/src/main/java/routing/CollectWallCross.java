package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;

public class CollectWallCross extends Routine {

    public CollectWallCross(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    @Override
    public void run() {
        avoidObstacle(10);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false);

        // Correct the robot for collection
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.collectRelease(true);
        super.robotController.drive(dest, true);
        super.robotController.stopCollectRelease();

        super.robotController.reverse();
    }
}
