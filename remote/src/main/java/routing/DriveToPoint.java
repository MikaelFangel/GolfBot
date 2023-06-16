package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import org.opencv.core.Point;


public class DriveToPoint extends Routine {

    public DriveToPoint(Point start, Point dest, Ball ballToCollect, Cross cross, RobotController robotController, RoutingController routingController) {
        super(start, dest, ballToCollect, cross, robotController, routingController);
    }

    @Override
    public void run() {
        avoidObstacle(0);

        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.drive(dest, true);
    }
}