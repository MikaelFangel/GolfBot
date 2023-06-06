import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.detection.DetectionController;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051)");
        }

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address

        int cameraIndex = 0;
        Course course = new Course();
        DetectionController detectionController = new DetectionController(course, cameraIndex, false);

        Ball closestBall = Algorithms.findClosestBall(course.getBalls(), course.getRobot());
        if (closestBall == null) return;

        double angle = Algorithms.findRobotsAngleToBall(course.getRobot(), closestBall);
        double distance = Algorithms.findRobotsDistanceToBall(course.getRobot(), closestBall);
        System.out.println("Driving distance: " + distance + "with angle: " + angle);

        Scanner scan  = new Scanner(System.in);
        System.out.println("Press ENTER to trigger robot");
        scan.nextLine();
    }
}
