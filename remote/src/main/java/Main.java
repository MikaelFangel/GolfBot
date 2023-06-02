import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.*;

import java.util.Scanner;

import static vision.Algorithms.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 2) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051) and Camera Index");
        }

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address
<<<<<<< HEAD
        Detection detection = new Detection(2);
=======
        Detection detection = new Detection(Integer.parseInt(args[1]));
>>>>>>> 63-correct-camera-view-to-actual-course-layout
        Course course = detection.getCourse();

        Ball closestBall = findClosestBall(course.getBalls(), course.getRobot());
        if (closestBall == null) return;

        double angle = findRobotsAngleToBall(course.getRobot(), closestBall);
        double distance = findRobotsDistanceToBall(course.getRobot(), closestBall);
        System.out.println("Driving distance: " + distance + "with angle: " + angle);


        Scanner scan  = new Scanner(System.in);
        System.out.println("Press ENTER to trigger robot");
        scan.nextLine();

        controller.rotate(angle);
        Thread.sleep(5000); // Find better way.
        controller.driveStraight(distance);

        controller.stopController();
    }
}
