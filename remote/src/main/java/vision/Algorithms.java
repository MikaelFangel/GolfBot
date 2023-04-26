package vision;

import courseObjects.Ball;
import courseObjects.Robot;
import org.opencv.core.Point;

import java.util.List;

import static vision.Calculations.angleBetweenTwoPoints;
import static vision.Calculations.distanceBetweenTwoPoints;

/**
 * Contains algorithms used to path find, find nearest ball etc.
 */
public class Algorithms {
    public static Ball findClosestBall(List<Ball> balls, Robot robot){
        if (balls.size() == 0 || robot == null) return null;
        Point robotCenter = robot.center;

        // Starting point
        Ball closestBall = balls.get(0);
        double closestDistance = distanceBetweenTwoPoints(
                closestBall.center.x,
                closestBall.center.y,
                robotCenter.x,
                robotCenter.y
        );

        // Find ball with least distance
        for (int i = 0; i < balls.size() ; i++) {
            Ball ball = balls.get(i);
            double distance = distanceBetweenTwoPoints(
                    ball.center.x,
                    ball.center.y,
                    robotCenter.x,
                    robotCenter.y
            );

            if (distance < closestDistance) {
                closestDistance = distance;
                closestBall = ball;
            }
        }

        return closestBall;
    }

    public static double findRobotsAngleToBall(Robot robot, Ball ball) {
        double angleToBall = angleBetweenTwoPoints(robot.center.x, robot.center.y, ball.center.x, ball.center.y);
        return robot.angle - angleToBall;
    }

    public static double findRobotsDistanceToBall(Robot robot, Ball ball) {
        return distanceBetweenTwoPoints(robot.center.x, robot.center.y, ball.center.x, ball.center.y);
    }
}
