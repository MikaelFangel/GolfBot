package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;

public class CollectWall extends Routine {

    public CollectWall(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    @Override
    public void run() {
        avoidObstacle(15);

        System.out.println("Wall: " + projectedPoint);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false, 80, 3);

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
