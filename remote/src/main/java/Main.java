import exceptions.MissingArgumentException;
import courseObjects.*;
import routing.RobotController;
import vision.Algorithms;
import vision.detection.DetectionConfiguration;
import vision.DetectionController;

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

        Routine.collectAllBallsRoutine(controller, course);

        System.out.println("Done");
    }
}
