package vision;

import courseObjects.Ball;
import courseObjects.Border;
import courseObjects.Robot;
import math.Geometry;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.stream.IntStream;

import static math.Geometry.angleBetweenTwoPoints;
import static math.Geometry.distanceBetweenTwoPoints;

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
     * Finds the shortest angle between robot and point.
     * @param robot current Point position
     * @param p next Point
     * @return shortest angle for rotation toward next point
     */
    public static double findRobotShortestAngleToPoint(Robot robot, Point p) {
        double clockWiseAngleToBall = angleBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, p.x, p.y);
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

    /**
     * Finds the shortest angle between robot and ball.
     *
     * @return angle in degrees. Clockwise with the robot returns positive angle-values, counter-clockwise with the robot returns negative angle-values.
     */
    public static double findRobotShortestAngleToBall(Robot robot, Ball ball) {
        return findRobotShortestAngleToPoint(robot, ball.getCenter());
    }

    /**
     * Finds distance between the robots front ball collection mechanism and a point.
     * @param robot Using the position of the robot
     * @param p The point to drive to
     * @param calculateFromFront False if calculating from the rear marker. True if calculating from the front marker
     * @return distance in cm
     */
    public static double findRobotsDistanceToPoint(Robot robot, Point p, boolean calculateFromFront) {
        // Distance from middle of front marker to middle of collecting front wheel in CM
        int offset = 0; // TODO: Check if offset is still needed
        if (calculateFromFront)
            return distanceBetweenTwoPoints(robot.getFront().x, robot.getFront().y, p.x, p.y) - offset;

        return distanceBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, p.x, p.y) - offset;
    }

    public static double findRobotsDistanceToBall(Robot robot, Ball ball) {
        return distanceBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y) - robot.length;
    }

    /**
     * Computes the actual cartesian coords of an object
     *
     * @param originalCoords Where the camera has detected the object
     * @param camera Point on surface perpendicular to the center of the camera
     * @param objectHeight The height of the object
     * @param cameraHeight Height the camera is from the course
     * @return the coordinate where the object in on the ground
     */
    public static Point correctedCoordinatesOfObject(Point originalCoords, Point camera, double objectHeight , double cameraHeight){
        //changing point, so that camera is ORIGO
        Point myCamera = new Point(0,0);
        Point myObject = new Point(originalCoords.x - camera.x, originalCoords.y - camera.y);

        // finds the angel between the camera and the point
        double angelToPointInDegree = Geometry.angleBetweenTwoPoints(myCamera.x, myCamera.y, myObject.x, myObject.y);

        // distance between objects and camera
        double distance = Geometry.distanceBetweenTwoPoints(myCamera.x, myCamera.y, myObject.x, myObject.y);

        // Finds the actual distance to the object
        double newDistance = Geometry.objectActualPosition(
                cameraHeight,
                objectHeight,
                distance
        );

        //Convert from polar coordinates to cartesian
        Point newPoint = new Point();
        newPoint.x = (Math.cos(Math.toRadians(angelToPointInDegree))*newDistance)+camera.x;
        newPoint.y = (Math.sin(Math.toRadians(angelToPointInDegree))*newDistance)+camera.y;

        return newPoint;
    }


    /**
     * Warp the picture based on the corners position, to remove what ever is outside the field.
     *
     * @param src the livefeed from the webcam
     * @param border that knows where the corners of the field is on the current livefeed.
     * @return A transformed picture, only containing the field, and not everything outside the frame.
     */
    public static Mat transformToRectangle(Mat src, Border border){
        //Offset that is needed, because the corners are the inner corners, and we would like to keep the borders in the frame.
        final double pixelOffset = 20;

        //Gets 3 of the 4 corners.
        Point[] srcTri = new Point[3];
        srcTri[0] = border.getTopLeft();
        srcTri[1] = border.getTopRight();
        srcTri[2] = border.getBottomLeft();

        //adds the offset to each corner
        IntStream.range(0,2).forEach(i -> {
            srcTri[i].x += i%2 == 0 ? -pixelOffset : pixelOffset;   //subtract on odd, and add on even
            srcTri[i].y += i/2*2 == 0 ? -pixelOffset : pixelOffset; //subtract on 0 and 1, and add on 2
        });

        //sets the end position of the pixels
        Point[] dstTri = new Point[3];
        dstTri[0] = new Point( 0, 0 );
        dstTri[1] = new Point( src.cols()-1, 0 );
        dstTri[2] = new Point( 0, src.rows()-1 );

        //Computes the new unit vector of the new frame
        MatOfPoint2f s = new MatOfPoint2f(srcTri);
        MatOfPoint2f d = new MatOfPoint2f(dstTri);
        Mat warpMat = Imgproc.getAffineTransform(s , d);

        //Creates the new Mat
        Mat warpDst = Mat.zeros(src.rows(), src.cols(), src.type());
        Imgproc.warpAffine(src, warpDst, warpMat, warpDst.size());

        return warpDst;
    }
}
