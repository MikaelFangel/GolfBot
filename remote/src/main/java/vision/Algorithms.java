package vision;

import courseObjects.Ball;
import courseObjects.Robot;
import org.opencv.core.Point;
import vision.math.Geometry;

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

        double shortestAngleToBall = clockWiseAngleToBall;

        // Check if there is a shorter angle
        if (clockWiseAngleToBall > 180)
            shortestAngleToBall = clockWiseAngleToBall - 360;

        return shortestAngleToBall;
    }

    public static double findRobotsDistanceToBall(Robot robot, Ball ball) {
        return distanceBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y) - robot.length;
    }

    public static Point correctedCoordinatesOfObject(Point originalCoords, Point camera, double objectHeight , double cameraHeight){
        //changing point, so that camera is origo
        Point myCamera = new Point(0, 0);
        Point myObject = new Point(originalCoords.x - camera.x, originalCoords.y - camera.y);

        //finds the angel between the camera and the point
        double angelToPointInDegree = Geometry.angleBetweenTwoPoints(myCamera.x, myCamera.y, myObject.x, myObject.y);

        //distance between objects
        double distance = Geometry.distanceBetweenTwoPoints(myCamera.x, myCamera.y, myObject.x, myObject.y);

        double newDistance = Geometry.objectActualPosition(
                cameraHeight,
                objectHeight,
                distance
        );

        Point newPoint = new Point();

        newPoint.x = (Math.cos(Math.toRadians(angelToPointInDegree)) * newDistance) + camera.x;
        newPoint.y = (Math.sin(Math.toRadians(angelToPointInDegree)) * newDistance) + camera.y;

        return newPoint;
    }
}
