import courseObjects.Ball;
import courseObjects.Course;
import exceptions.MissingArgumentException;
import routing.RobotController;
import routing.RoutingController;
import vision.Algorithms;
import vision.DetectionController;
import vision.detection.DetectionConfiguration;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 2) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051) and camera index");
        }

        int cameraIndex = Integer.parseInt(args[1]);
        double cameraHeight = Double.parseDouble(args[2]);  // in cm


        Course course = new Course(cameraHeight);
        RobotController controller = new RobotController(args[0], course.getRobot()); // Args[0] being and IP address

        DetectionConfiguration.DetectionConfiguration();
        new DetectionController(course, cameraIndex, false);

        RoutingController routingController = new RoutingController(course, args[0]);

        routingController.stopCurrentRoute();

        Thread.sleep(2000);
        while (!course.getBalls().isEmpty()) {
            Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());

            // This check also make sure program won't crash after collecting last ball
            if (closestBall == null)
                break;

            System.out.println("Chosen collection strat: " + closestBall.getStrategy().toString());
            routingController.addRoutine(closestBall);
            routingController.driveRoutes();
        }

        System.out.println("Done");
    }
}
