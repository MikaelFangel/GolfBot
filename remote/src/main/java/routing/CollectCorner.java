package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import org.opencv.core.Point;

public class CollectCorner extends Routine {

    public CollectCorner(Point start, Point dest, Ball ballToCollect, Cross cross, RobotController robotController, RoutingController routingController) {
        super(start, dest, ballToCollect, cross, robotController, routingController);
    }

    @Override
    public void run() {
        avoidObstacle(10);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false);


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
