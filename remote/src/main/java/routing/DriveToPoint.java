package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;


public class DriveToPoint extends Routine {

    public DriveToPoint(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    @Override
    public void run() {
        avoidObstacle(0);

        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.drive(dest, true, 100, 3);
    }
}
