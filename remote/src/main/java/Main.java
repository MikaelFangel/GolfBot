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
        while(true) {
            try {
                while (true) {
                    routePlanner.computeFullRoute(course, controller.getRobot().getNumberOfBallsInMagazine());
                    routePlanner.getComputedRoute(routingController);
                    controller.startMagazineCounting(course.getBalls().size());
                    routingController.driveRoutes();
                    controller.endMagazineCounting(course.getBalls().size());
                }
            } catch (Exception e) {
                System.out.println(e);
                routingController.stopCurrentRoute();
                controller.recalibrateGyro();
                controller.reverse();
            }
        }
    }

    public static void reset(RobotController robotController) {
        robotController.stopCollectRelease();
        robotController.stopMotors();
    }
}
