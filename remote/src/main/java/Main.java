import exceptions.MissingArgumentException;
import courseObjects.*;
import routing.RobotController;
import vision.Algorithms;
import vision.detection.DetectionConfiguration;
import vision.DetectionController;

public class Main {
    public static void main(String[] args) throws MissingArgumentException, InterruptedException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide a camera index");
        }

        int cameraIndex = Integer.parseInt(args[0]);

        RobotController controller = new RobotController();

        Course course = new Course();
        new DetectionController(course, cameraIndex, false); // Runs in the background

        DetectionConfiguration.DetectionConfiguration();

        Routine.collectAllBallsRoutine(controller, course);

        System.out.println("Done");
    }
}
