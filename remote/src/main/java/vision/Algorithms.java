package vision;

import courseObjects.Ball;
import courseObjects.Border;
import courseObjects.Course;
import courseObjects.Robot;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import vision.helperClasses.ClassScope;
import vision.math.Geometry;

import java.util.Arrays;
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

    /**
     * Finds the shortest angle between robot and ball.
     * @param robot
     * @param ball
     * @return angle in degrees. Clockwise with the robot returns positive angle-values, counter-clockwise with the robot returns negative angle-values.
     */
    public static double findRobotsAngleToBall(Robot robot, Ball ball) {
        double clockWiseAngleToBall = angleBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y);

        double shortestAngleToBall = clockWiseAngleToBall;

        // Check if there is a shorter angle
        if (clockWiseAngleToBall > 180)
            shortestAngleToBall = clockWiseAngleToBall - 360;
        else
            shortestAngleToBall = robot.getAngle() - clockWiseAngleToBall;

        return shortestAngleToBall;
    }

    public static double findRobotsDistanceToBall(Robot robot, Ball ball) {
        return distanceBetweenTwoPoints(robot.getCenter().x, robot.getCenter().y, ball.getCenter().x, ball.getCenter().y) - robot.length;
    }

    public static Point correctedCoordinatesOfObject(Point originalCoords, Point camera, double objectHeight , double cameraHeight){
        //changing point, so that camera is origo
        Point myCamera = new Point(0,0);
        Point myObject = new Point(originalCoords.x - camera.x, originalCoords.y - camera.y);

        // finds the angel between the camera and the point
        double angelToPointInDegree = Geometry.angleBetweenTwoPoints(myCamera.x, myCamera.y, myObject.x, myObject.y);

        // distance between objects
        double distance = Geometry.distanceBetweenTwoPoints(myCamera.x, myCamera.y, myObject.x, myObject.y);

        double newDistance = Geometry.objectActualPosition(
                cameraHeight,
                objectHeight,
                distance
        );

        Point newPoint = new Point();

        newPoint.x = (Math.cos(Math.toRadians(angelToPointInDegree))*newDistance)+camera.x;
        newPoint.y = (Math.sin(Math.toRadians(angelToPointInDegree))*newDistance)+camera.y;

        return newPoint;
    }


    public static Mat transformToRectangle(Mat src, Border border){
        final double pixelOffset = 40;

        Point[] srcTri = new Point[3];

        srcTri[0] = border.getTopLeft().clone();
        srcTri[1] = border.getTopRight().clone();
        srcTri[2] = border.getBottomLeft().clone();

        // Add offset
        srcTri[0] = new Point(srcTri[0].x - pixelOffset, srcTri[0].y - pixelOffset);
        srcTri[1] = new Point(srcTri[1].x + pixelOffset, srcTri[1].y - pixelOffset);
        srcTri[2] = new Point(srcTri[2].x - pixelOffset, srcTri[2].y + pixelOffset);

        // Swap BottomLeft and TopRight
        if (srcTri[1].x > srcTri[2].x){
            Point temp = srcTri[1];
            srcTri[1] = srcTri[2];
            srcTri[2] = temp;
        }



        Point[] dstTri = new Point[3];
        dstTri[0] = new Point( 0, 0 );
        dstTri[1] = new Point( 0, src.rows()-1 );
        dstTri[2] = new Point( src.cols()-1, 0 );

        MatOfPoint2f s = new MatOfPoint2f(srcTri);
        MatOfPoint2f d = new MatOfPoint2f(dstTri);
        Mat warpMat = Imgproc.getAffineTransform(s , d);

        Mat warpDst = Mat.zeros(src.rows(), src.cols(), src.type());

        Imgproc.warpAffine(src, warpDst, warpMat, warpDst.size());

        // Release OpenCV items
        /*
        warpMat.release();
        warpDst.release();
        s.release();
        d.release();
         */

        return warpDst;
    }
}
