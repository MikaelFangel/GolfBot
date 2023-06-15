package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import org.opencv.core.Point;

public class CollectWallCross extends Routine {

    public CollectWallCross(Point start, Point dest, Ball ballToCollect, Cross cross, RobotController robotController, RoutingController routingController) {
        super(start, dest, ballToCollect, cross, robotController, routingController);
    }

    @Override
    public void run() {
        avoidObstacle(14);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint);

        //
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.collectRelease(true);
        super.robotController.drive(dest);
        super.robotController.stopCollectRelease();

        // TODO: Reverse away from wall
    }
}
