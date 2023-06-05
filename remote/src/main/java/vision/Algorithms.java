package vision;

import courseObjects.Ball;
import courseObjects.Course;
import courseObjects.Robot;
import org.opencv.core.Point;
import vision.math.Geometry;

import java.util.List;

import static vision.math.Geometry.angleBetweenTwoPoints;
import static vision.math.Geometry.distanceBetweenTwoPoints;

/**
 * Contains algorithms used to path find, find nearest ball etc.
 */
public class Algorithms {
    public static Ball findClosestBall(List<Ball> balls, Robot robot){
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

    public static double findRobotsAngleToBall(Robot robot, Ball ball) {
        double angleToBall = angleBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y);
        return robot.getAngle() - angleToBall;
    }

    public static double findRobotsDistanceToBall(Robot robot, Ball ball) {
        return distanceBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y) - robot.length;
    }

    public static Point correctedCoordinatesOfObject(Point originalCoords, double objectHeight , Course course, double conversionFactor, Point camera){
        //finds the angel between the camera and the point
        double angelToPoint = Geometry.angleBetweenTwoPoints(camera.x, camera.y,originalCoords.x,originalCoords.y);

        //distanec between objects
        double distance = Geometry.distanceBetweenTwoPoints(camera.x, camera.y, originalCoords.x, originalCoords.y);

        double newDistance = Geometry.objectActualPosition(
                course.getCameraHeight(),
                objectHeight,
                conversionFactor * distance
        ) / conversionFactor;

        Point newPoint = new Point();

        newPoint.x = Math.pow(Math.cos(angelToPoint),2)*newDistance;
        newPoint.y = Math.pow(Math.sin(angelToPoint),2)*newDistance;

        if (camera.x >= originalCoords.x) newPoint.x = camera.x - newPoint.x;
        else newPoint.x = camera.x + newPoint.x;

        if (camera.y >= originalCoords.y) newPoint.y = camera.y - newPoint.y;
        else newPoint.y = camera.y + newPoint.y;

        return newPoint;
    }
}
