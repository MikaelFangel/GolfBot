import exceptions.MissingArgumentException;
import courseObjects.*;
import routing.RobotController;
import routing.Routine;
import vision.Algorithms;
import vision.detection.DetectionConfiguration;
import vision.DetectionController;

public class Main {
    public static void main(String[] args) throws MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide a camera index");
        }

        int cameraIndex = Integer.parseInt(args[0]);

        Course course = new Course();
        DetectionController detectionController = new DetectionController(course, cameraIndex, true);

        RobotController controller = new RobotController(course.getRobot()); // Args[0] being and IP address

        DetectionConfiguration.DetectionConfiguration();
        new DetectionController(course, cameraIndex, false);

        //Routine.collectAllBallsRoutine(controller, course);

        System.out.println("Done");
    }
}
