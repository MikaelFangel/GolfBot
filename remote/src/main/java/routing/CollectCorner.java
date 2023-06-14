package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import org.opencv.core.Point;
import vision.Algorithms;

public class CollectCorner extends Routine {
    private Ball ballToCollect;
    public CollectCorner(Point start, Point dest, Cross cross, RobotController robotController, RoutingController routingController, Ball ballToCollect) {
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

        // TODO: Reverse out of corner
    }
}
