package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;

public class DeliverBallsToGoal extends Routine {


    public DeliverBallsToGoal(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
    }

    /**
     * Routine for driving to goal and releasing balls
     */
    @Override
    public void run() {
        avoidObstacle(0);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false);

        // Correct the robot release to goal
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));

        releaseAllBalls();

        super.robotController.reverse();
    }

    private void releaseAllBalls() {
        super.robotController.collectRelease(false);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.robotController.stopCollectRelease();
    }
}
