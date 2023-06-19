package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;

public class CollectCorner extends Routine {

    public CollectCorner(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    @Override
    public void run() {
        avoidObstacle(10);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false, 80, 3);


        // Run shooting routine
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));

        super.robotController.releaseOneBall();
        super.robotController.collectRelease(true);

        try {
            Thread.sleep(2000); // Can be adjusted. How long we collect
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        super.robotController.stopCollectRelease();

        super.robotController.reverse();
    }
}
