import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.Algorithms;
import vision.detection.DetectionConfiguration;
import vision.DetectionController;

public class Main {
    public static void main(String[] args) throws MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide a camera index");
        }

        int cameraIndex = Integer.parseInt(args[0]);

        RobotController controller = new RobotController();

        Course course = new Course();
        new DetectionController(course, cameraIndex, true); // Runs in the background

        DetectionConfiguration.DetectionConfiguration();

        while (true) {

            Ball closestBall;

            // Check if there is balls left
            if(course.getBalls() != null) {
                closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

                // Do we need both checks? This check also make sure program won't crash after collecting last ball
                if (closestBall == null)
                    break;
            } else {
                // No balls left on the course. TODO: Should drive to drop off point
                break;
            }

            double angle = Algorithms.findRobotShortestAngleToBall(course.getRobot(), closestBall);
            double distance = Algorithms.findRobotsDistanceToBall(course.getRobot(), closestBall);
            System.out.println("Driving distance: " + distance + " with angle: " + angle);

            // Quick integration test, rotate to the ball and collect it
            controller.recalibrateGyro();
            controller.rotateWGyro(-angle);
            controller.collectRelease(true);
            controller.recalibrateGyro();
            controller.driveWGyro(course);
            controller.stopCollectRelease();
        }

        System.out.println("Done");
    }

}
