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

        while (true) {
            Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            if (controller.getRobot().getNumberOfBallsInMagazine() > 4)
                routingController.addRoutine(course.getBorder().getSmallGoalMiddlePoint(), true);
            else if (closestBall != null) {
                routingController.addRoutine(closestBall);
                System.out.println("Chosen collection strategy: " + closestBall.getStrategy().toString());
                controller.startMagazineCounting(course.getBalls().size()); // Start ball counting
            }

            routingController.driveRoutes();

            if (closestBall != null)
                controller.endMagazineCounting(course.getBalls().size()); // Stop ball counting

            System.out.println("Balls in robot: " + controller.getRobot().getNumberOfBallsInMagazine());
        }
    }

    public static void reset(RobotController robotController) {
        robotController.stopCollectRelease();
        robotController.stopMotors();
    }
}
