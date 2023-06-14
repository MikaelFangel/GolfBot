package routing;

import courseObjects.Cross;
import org.opencv.core.Point;

public class DriveAndCollect extends Routine{
    public DriveAndCollect(Point start, Point dest, Cross cross, RobotController robotController, RoutingController routingController) {
        super(start, dest, cross, robotController, routingController);
    }

    @Override
    public void run() {
        if (super.isRouteObstructed(super.start, super.dest)) {
            Point intermediate = getIntermediatePointForObstructedRoute(super.start, super.dest);

            super.robotController.recalibrateGyro();
            super.robotController.rotate(super.getDegreesToTurn(intermediate));
            super.robotController.recalibrateGyro();
            super.robotController.drive(intermediate);
        }

        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.collectRelease(true);
        super.robotController.drive(dest);
        super.robotController.stopCollectRelease();
    }
}
