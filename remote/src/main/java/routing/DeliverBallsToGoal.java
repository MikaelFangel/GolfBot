package routing;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;

public class DeliverBallsToGoal extends Routine {

    private final Point intermediatePoint;
    public DeliverBallsToGoal(Point start, Point dest, Ball ballToCollect, RobotController robotController, RoutingController routingController, Course course) {
        super(start, dest, ballToCollect, robotController, routingController, course);
        this.intermediatePoint = intermediatePoint;
    }

    /**
     * Routine for driving to goal and releasing balls
     */
    @Override
    public void run() {
        avoidObstacle(0);

        // Drive to intermediate stop before projection to start from a more precise point
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(this.intermediatePoint));
        super.robotController.recalibrateGyro();
        super.robotController.drive(this.intermediatePoint, false, 80, 3);

        // Drive to a projected spot
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.projectedPoint));

        super.robotController.recalibrateGyro();
        super.robotController.drive(super.projectedPoint, false, 30, 10);

        // Correct the robot release to goal
        super.robotController.recalibrateGyro();
        super.robotController.rotate(super.getDegreesToTurn(super.dest));

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
