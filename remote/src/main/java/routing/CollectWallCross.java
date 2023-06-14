package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import org.opencv.core.Point;

public class CollectWallCross extends Routine {
    private Ball ballToCollect;
    public CollectWallCross(Point start, Point dest, Cross cross, RobotController robotController, RoutingController routingController, Ball ballToCollect) {
        super(start, dest, cross, robotController, routingController);
        this.ballToCollect = ballToCollect;
    }

    @Override
    public void run() {
        Point projectedPoint;
        if (this.ballToCollect != null)
            projectedPoint = super.routingController.projectPoint(ballToCollect, 14);
        else
            return;

        // Avoid obstacles
        if (super.isRouteObstructed(super.start, projectedPoint)) {
            Point intermediate = getIntermediatePointForObstructedRoute(super.start, projectedPoint);

            super.robotController.recalibrateGyro();
            super.robotController.rotate(super.getDegreesToTurn(intermediate));
            super.robotController.recalibrateGyro();
            super.robotController.drive(intermediate);
        }

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(projectedPoint);

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
