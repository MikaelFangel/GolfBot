package routing;

import courseObjects.Ball;
import courseObjects.Cross;
import org.opencv.core.Point;

public class DeliverBallsToGoal extends Routine {

    public DeliverBallsToGoal(Point start, Point dest, Ball ballToCollect, Cross cross, RobotController robotController, RoutingController routingController) {
        super(start, dest, ballToCollect, cross, robotController, routingController);
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
        super.robotController.drive(super.projectedPoint, false, 30, 10);

        // Correct the robot release to goal
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));

        // TODO: Check if this is really needed for always being precise
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(dest));

        releaseAllBalls();

        super.robotController.reverse();

        // Set magazine to empty
        super.robotController.getRobot().setNumberOfBallsInMagazine(0);
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
