import courseObjects.Ball;
import courseObjects.Course;
import exceptions.MissingArgumentException;
import routing.Algorithm.HamiltonianRoute;
import routing.Algorithm.IRoutePlanner;
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
        IRoutePlanner routePlanner = new HamiltonianRoute();
        //routingController.addRoutine(course.getRobot().getCenter(), true);
        //routingController.driveRoutes();
        while (!course.getBalls().isEmpty()) {
            routePlanner.computeFullRoute(course,1);
            routePlanner.getComputedRoute(routingController);
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
