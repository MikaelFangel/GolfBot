import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.Algorithms;
import vision.DetectionController;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 2) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051) and camera index");
        }

        int cameraIndex = Integer.parseInt(args[1]);
        double cameraHeight = 192;  // in cm TODO make into arg at some point

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address

        Course course = new Course(cameraHeight);
        new DetectionController(course, cameraIndex, false);

        //collectBallRoutine(controller, course);
        //controller.collectRelease(false);
        //Thread.sleep(3000);
        //controller.stopCollectRelease();
        driveRoutine(controller, course);

        System.out.println("Done");
    }

    public static void rotateRoutine(RobotController controller, Course course) {
        Ball closestBall;

        // Check if there is balls left
        if (course.getBalls() != null) {
            // Use new Target object
            closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            // Do we need both checks? This check also make sure program won't crash after collecting last ball
            if (closestBall == null)
                return;
        } else {
            // No balls left on the course. TODO: Should drive to drop off point
            return;
        }

        double angle = Algorithms.findRobotShortestAngleToBall(course.getRobot(), closestBall);
        controller.recalibrateGyro();
        controller.rotateWGyro(angle);
    }

    public static void driveRoutine(RobotController controller, Course course) {
        Ball closestBall;

        // Check if there is balls left
        if (course.getBalls() != null) {
            // Use new Target object
            closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            // Do we need both checks? This check also make sure program won't crash after collecting last ball
            if (closestBall == null)
                return;
        } else {
            // No balls left on the course. TODO: Should drive to drop off point
            return;
        }

        //double angle = Algorithms.findRobotShortestAngleToBall(course.getRobot(), closestBall);
        //System.out.println("Degrees to rotate " + angle);

        // Quick integration test, rotate to the ball and collect it
        controller.recalibrateGyro();
        controller.driveWGyro(course, closestBall.getCenter());
    }

    public static void collectAllBallsRoutine(RobotController controller, Course course) throws InterruptedException {
        while (!course.getBalls().isEmpty()) {

            for (Ball ball : course.getBalls())
                System.out.println(ball);

            // Use new Target object
            Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            // Do we need both checks? This check also make sure program won't crash after collecting last ball
            if (closestBall == null)
                break;

            double angle = Algorithms.findRobotShortestAngleToBall(course.getRobot(), closestBall);
            System.out.println("Degrees to rotate " + angle);

            // Quick integration test, rotate to the ball and collect it
            controller.recalibrateGyro();
            controller.rotateWGyro(angle);
            controller.recalibrateGyro();
            controller.collectRelease(true);
            controller.driveWGyro(course, closestBall.getCenter());
            controller.stopCollectRelease();
            Thread.sleep(300);
        }
    }
}
