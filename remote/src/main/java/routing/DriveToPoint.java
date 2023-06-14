package routing;

import courseObjects.Cross;
import math.Geometry;
import org.opencv.core.Point;
import vision.Algorithms;


public class DriveToPoint extends Routine {
    public DriveToPoint(Point start, Point dest, Cross cross, RobotController robotController, RoutingController routingController) {
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
        super.robotController.drive(dest);
    }
}
