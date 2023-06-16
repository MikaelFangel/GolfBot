import courseObjects.Ball;
import courseObjects.Course;
import exceptions.MissingArgumentException;
import routing.RobotController;
import routing.RoutingController;
import vision.Algorithms;
import vision.DetectionController;
import vision.detection.DetectionConfiguration;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws MissingArgumentException, InterruptedException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide a camera index");
        }

        int cameraIndex = Integer.parseInt(args[0]);
        Course course = new Course();
        new DetectionController(course, cameraIndex, false); // Runs in the background
        DetectionConfiguration.DetectionConfiguration();

        RobotController controller = new RobotController(course.getRobot());
        reset(controller);
        controller.recalibrateGyro();

        JOptionPane.showMessageDialog(null, "Continue when vision setup is done");

        RoutingController routingController = new RoutingController(course);
        while (!course.getBalls().isEmpty()) {
            Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            // This check also make sure program won't crash after collecting last ball
            if (closestBall == null)
                break;

            System.out.println("Chosen collection strategy: " + closestBall.getStrategy().toString());
            routingController.addRoutine(closestBall);
            routingController.driveRoutes();
        }

        routingController.addRoutine(course.getBorder().getSmallGoalMiddlePoint(), true);
        routingController.driveRoutes();

        System.out.println("Done");
    }

    public static void reset(RobotController robotController) {
        robotController.stopCollectRelease();
        robotController.stopMotors();
    }
}
