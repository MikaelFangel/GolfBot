package vision;

import courseObjects.Ball;
import courseObjects.Robot;
import org.opencv.core.Point;

import java.util.List;

import static vision.math.Geometry.angleBetweenTwoPoints;
import static vision.math.Geometry.distanceBetweenTwoPoints;

/**
 * Contains algorithms used to path find, find the nearest ball etc.
 */
public class Algorithms {
    public static Ball findClosestBall(List<Ball> balls, Robot robot) {
        if (balls.size() == 0 || robot == null) return null;
        Point robotCenter = robot.getCenter();

        // Starting point
        Ball closestBall = balls.get(0);
        double closestDistance = distanceBetweenTwoPoints(
                closestBall.getCenter().x,
                closestBall.getCenter().y,
                robotCenter.x,
                robotCenter.y
        );

        // Find ball with the least distance
        for (Ball ball : balls) {
            double distance = distanceBetweenTwoPoints(
                    ball.getCenter().x,
                    ball.getCenter().y,
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

    /**
     * Finds the shortest angle between robot and ball.
     *
     * @return angle in degrees. Clockwise with the robot returns positive angle-values, counter-clockwise with the robot returns negative angle-values.
     */
    public static double findRobotShortestAngleToBall(Robot robot, Ball ball) {
        double clockWiseAngleToBall = angleBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y);
        clockWiseAngleToBall -= robot.getAngle();

        double shortestAngleToBall = clockWiseAngleToBall;

        // Check if there is a shorter angle
        if (clockWiseAngleToBall > 180)
            shortestAngleToBall = clockWiseAngleToBall - 360;
        else if (clockWiseAngleToBall < -180) {
            shortestAngleToBall = clockWiseAngleToBall + 360;
        }

        return shortestAngleToBall;
    }

    public static double findRobotsDistanceToBall(Robot robot, Ball ball) {
        return distanceBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y) - robot.length;
    }
}
