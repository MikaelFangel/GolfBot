import courseObjects.Ball;
import courseObjects.Border;
import courseObjects.Course;
import courseObjects.Robot;
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
            controller.drive(course.getRobot(), closestBall.getCenter(), true);
            controller.stopCollectRelease();
            // Previous call is non-blocking, and we have to wait for it to end before sending new instruction
            Thread.sleep(300);
        }
    }
    public static void driveToBall(RobotController controller, Course course) throws InterruptedException {
        Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

        // This check also make sure program won't crash after collecting last ball
        if (closestBall == null)
            return;

        double angle = Algorithms.findRobotShortestAngleToBall(course.getRobot(), closestBall);

        // rotate to the ball and collect it
        controller.recalibrateGyro();
        controller.rotate(angle);
        controller.recalibrateGyro();
        controller.drive(course.getRobot(), closestBall.getCenter(), true);
        // Previous call is non-blocking, and we have to wait for it to end before sending new instruction
        Thread.sleep(300);
    }
    private static void releaseAllBalls(RobotController controller) throws InterruptedException {
        controller.collectRelease(false);
        Thread.sleep(3000);
        controller.stopCollectRelease();
    }

    /**
     * Routine for driving to goal and releasing balls
     * @param controller Controlling the robot
     * @param course Holding the robot and border
     * @throws InterruptedException Needed for releaseAllBalls
     */
    public static void goal(RobotController controller, Course course) throws InterruptedException {
        Robot robot = course.getRobot();
        Border border = course.getBorder();

        var angleToStopover = Algorithms.findRobotShortestAngleToPoint(robot, border.getSmalGoalDestPoint());
        controller.recalibrateGyro();
        controller.rotate(angleToStopover);

        controller.recalibrateGyro();
        controller.drive(robot, border.getSmalGoalDestPoint(), false);

        var angleToGoal = Algorithms.findRobotShortestAngleToPoint(robot, border.getSmallGoalMiddlePoint());
        controller.recalibrateGyro();
        controller.rotate(angleToGoal);

        releaseAllBalls(controller);
    }
}
