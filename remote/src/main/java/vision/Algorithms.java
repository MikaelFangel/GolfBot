package vision;

import courseObjects.Ball;
import courseObjects.Robot;
import org.opencv.core.Point;

import static vision.Calculations.angleBetweenTwoPoints;
import static vision.Calculations.distanceBetweenTwoPoints;

/**
 * Contains algorithms used to path find, find nearest ball etc.
 */
public class Algorithms {
    public static Ball findClosestBall(Ball[] balls, Robot robot){
        if (balls.length == 0 || robot == null) return null;
        Point robotCenter = robot.center;

        // Starting point
        Ball closestBall = balls[0];
        double closestDistance = distanceBetweenTwoPoints(
                closestBall.center.x,
                closestBall.center.y,
                robotCenter.x,
                robotCenter.y
        );

        // Find ball with least distance
        for (int i = 0; i <balls.length ; i++) {
            Ball ball = balls[i];
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
