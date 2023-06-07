import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.Algorithms;
import vision.DetectionController;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051)");
        }

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address

        int cameraIndex = 0;
        DetectionController detectionController = new DetectionController(cameraIndex, false);

        Course course = detectionController.getCourse();

        Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());
        if (closestBall == null) return;

        double angle = Algorithms.findRobotsAngleToBall(course.getRobot(), closestBall);
        double distance = Algorithms.findRobotsDistanceToBall(course.getRobot(), closestBall);
        System.out.println("Driving distance: " + distance + "with angle: " + angle);

        // Integration test, rotate to the ball and collect it
        controller.recalibrateGyro();
        controller.rotateWGyro(-angle);
        controller.collectRelease(true);
        controller.recalibrateGyro();
        controller.driveWGyro(course);
        controller.stopCollectRelease();
    }
}