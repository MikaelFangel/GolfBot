import courseObjects.Ball;
import courseObjects.Course;
import routing.RobotController;
import vision.Algorithms;

public class Routine {
    public static void collectAllBallsRoutine(RobotController controller, Course course) throws InterruptedException {
        // Quick integration test, collect until no balls are left
        while (!course.getBalls().isEmpty()) {
            Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            // This check also make sure program won't crash after collecting last ball
            if (closestBall == null)
                break;

            double angle = Algorithms.findRobotShortestAngleToBall(course.getRobot(), closestBall);

            // rotate to the ball and collect it
            controller.recalibrateGyro();
            controller.rotate(angle);
            controller.recalibrateGyro();
            controller.collectRelease(true);
            controller.drive(closestBall.getCenter());
            controller.stopCollectRelease();
            // Previous call is non-blocking, and we have to wait for it to end before sending new instruction
            Thread.sleep(300);
        }
    }
    public static void releaseAllBalls(RobotController controller) throws InterruptedException {
        controller.collectRelease(false);
        Thread.sleep(3000);
        controller.stopCollectRelease();
    }
}
