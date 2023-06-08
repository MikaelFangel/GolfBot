import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.DetectionController;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 2) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051) and camera index");
        }

        String ipaddr = args[0];
        int cameraIndex = Integer.parseInt(args[1]);
        double cameraHeight = 168.2;  // in cm TODO make into arg at some point

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address

        Course course = new Course(cameraHeight);
        DetectionController detectionController = new DetectionController(course, cameraIndex, true);
    }
}
