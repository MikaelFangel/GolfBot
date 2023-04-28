import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.*;
import static vision.Algorithms.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        /*if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051)");
        }*/

        RobotController controller = new RobotController("192.168.1.12:50051");
        Detection detection = new Detection(2);
        Course course = detection.getCourse();

        Ball closestBall = findClosestBall(course.getBalls(), course.getRobot());
        if (closestBall == null) return;

        double angle = findRobotsAngleToBall(course.getRobot(), closestBall);
        double distance = findRobotsDistanceToBall(course.getRobot(), closestBall);

        //controller.rotate(angle);
        //Thread.sleep(10000); // Find better way.
        //controller.driveStraight(distance);

        controller.stopController();


    }
}
