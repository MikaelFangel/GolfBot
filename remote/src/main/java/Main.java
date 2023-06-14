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
        new DetectionController(course, cameraIndex, false);

        Routine.collectAllBallsRoutine(controller, course);
        //Routine.releaseAllBalls(controller);
        //Routine.driveToBall(controller, course);
        //controller.stopMotors();
        controller.stopCollectRelease();

        System.out.println("Done");
    }
}
