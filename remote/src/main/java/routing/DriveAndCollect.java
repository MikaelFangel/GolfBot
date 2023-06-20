package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;
import vision.BallPickupStrategy;

public class DriveAndCollect extends Routine{


    public DriveAndCollect(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    @Override
    public void run() {
        avoidObstacle(0);

        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));
        super.robotController.recalibrateGyro();
        super.robotController.collectRelease(true);
        super.robotController.drive(dest, true, 100, 3);
        super.robotController.stopCollectRelease();

        //if(super.ballToCollect.getStrategy() != BallPickupStrategy.FREE)
            super.robotController.reverse();
    }
}
