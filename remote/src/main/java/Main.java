import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.*;
import vision.detection.CourseDetector;

import java.util.Scanner;

import static vision.Algorithms.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051)");
        }

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address

        int cameraIndex = 2;
        Course course = new Course();
        CourseDetector courseDetector = new CourseDetector(course, cameraIndex, true);
    }
}
