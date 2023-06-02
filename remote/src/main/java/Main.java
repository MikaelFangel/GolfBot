import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.*;

import java.util.Scanner;

import static vision.Algorithms.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051)");
        }

        RobotController controller = new RobotController(args[0]); // Args[0] being and IP address
        Detection detection = new Detection(0);
        Course course = detection.getCourse();

        Ball closestBall = findClosestBall(course.getBalls(), course.getRobot());
        if (closestBall == null) return;

        double angle = findRobotsAngleToBall(course.getRobot(), closestBall);
        double distance = findRobotsDistanceToBall(course.getRobot(), closestBall);
        System.out.println("Driving distance: " + distance + "with angle: " + angle);


        Scanner scan  = new Scanner(System.in);
        System.out.println("Press ENTER to trigger robot");
        scan.nextLine();

        Thread.sleep(5000); // Find better way.
        controller.driveStraight(distance);

        controller.stopController();
    }
}
