import nu.pattern.OpenCV;
import org.opencv.core.Mat;

import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import vision.helperClasses.BorderSet;

import static vision.Detection.*;

public class IntegrationMain {
    public static void main(String[] args) throws InterruptedException {
        int realWidth = 167;
        int realHeight = 122;

        // Initialize library
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture();
        capture.open(2); // Might need to be changed
        // capture.open("/home/frederik/Desktop/border.mp4");

        RobotController robotController = new RobotController("192.168.1.12:50051");

        // Main Loop
        while (true) {
            if (capture.isOpened()) {
                Mat frame = new Mat();
                capture.read(frame);

                if (frame.empty()) break;

                Point[] ballCoords = getCircleCoordsFromFrame(frame);
                Point[] robotMarkerCoords = getRotationCoordsFromFrame(frame); // Used for rotation

                // Get border coords
                Point topLeft = null, topRight = null, bottomRight = null, bottomLeft = null;
                Point irlTopLeft = null, irlTopRight = null, irlBottomLeft = null, irlBottomRight = null;

                BorderSet borderSet = getBorderFromFrame(frame);

                double conversionFactor = 0;
                if (borderSet != null) {
                    Point[] cornerCoords = borderSet.correctCoords;
                    Point origin = borderSet.origin;

                    topLeft = new Point(cornerCoords[0].x, cornerCoords[0].y);
                    topRight = new Point(cornerCoords[1].x, cornerCoords[1].y);
                    bottomRight = new Point(cornerCoords[2].x, cornerCoords[2].y);
                    bottomLeft = new Point(cornerCoords[3].x, cornerCoords[3].y);

                    // Get irl coordinates
                    conversionFactor = realWidth / distanceBetweenTwoPoints(topLeft.x, topLeft.y, topRight.x, topRight.y);

                    irlTopLeft = new Point(cornerCoords[0].x * conversionFactor, cornerCoords[0].y * conversionFactor);
                    irlTopRight = new Point(cornerCoords[1].x * conversionFactor, cornerCoords[1].y * conversionFactor);
                    irlBottomRight = new Point(cornerCoords[2].x * conversionFactor, cornerCoords[2].y * conversionFactor);
                    irlBottomLeft = new Point(cornerCoords[3].x * conversionFactor, cornerCoords[3].y * conversionFactor);
                }

                if (ballCoords.length != 0 && robotMarkerCoords.length != 0 && conversionFactor != 0) {
                    Point origin = borderSet.origin;

                    // Find the closest ball
                    Point centerMarker = robotMarkerCoords[0];
                    Point rotationMarker = robotMarkerCoords[1];
                    Point closestBall = findClosestBall(ballCoords, centerMarker);

                    // Correct Coords from origin
                    //TODO These 4 have not been tested
                    centerMarker.x -= origin.x;
                    centerMarker.y -= origin.y;
                    rotationMarker.x -= origin.x;
                    rotationMarker.y -= origin.y;

                    for (Point ball : ballCoords) {
                        ball.x -= origin.x;
                        ball.y -= origin.y;
                    }

                    // Calculate polar coordinates to get rotation.
                    double distance = distanceBetweenTwoPoints(centerMarker.x, centerMarker.y, closestBall.x, closestBall.y);

                    double angleRobot = angleBetweenTwoPoints(centerMarker.x, centerMarker.y, rotationMarker.x, rotationMarker.y);
                    double angleBall = angleBetweenTwoPoints(centerMarker.x, centerMarker.y, closestBall.x, closestBall.y);

                    double angleDiff = angleBall - angleRobot;
                    System.out.println("DiffAngle: " + angleDiff);
                    System.out.println("DiffDistance " + distance*conversionFactor);

                    /*
                    // TODO Make robot drive to closest ball
                    System.out.println("Got Everything. Will proceed");;

                    System.out.println("Rotating towards ball...");
                    robotController.rotate(angleDiff);
                    Thread.sleep(5000);

                    System.out.println("Driving towards ball");
                    robotController.driveStraight(distance);
                    Thread.sleep(5000);

                    System.out.println("Should have arrived");
                    break;
                    */

                }

                // Pure debugging
                {
                    System.out.println("\n### New Frame ###");

                    System.out.println("-- Pixel Coords --");

                    for (Point ball : ballCoords) {
                        System.out.println("Circle: " + ball.x + ", " + ball.y);
                    }

                    System.out.println(" ");
                    for (Point marker : robotMarkerCoords) {
                        System.out.println("marker: " + marker.x + ", " + marker.y);
                    }
                    System.out.println(" ");

                    if (topLeft != null) { // They will all be not null at the same time
                        System.out.println("TopLeft: " + topLeft.x + ", " + topLeft.y);
                        System.out.println("TopRight: " + topRight.x + ", " + topRight.y);
                        System.out.println("BottomLeft: " + bottomLeft.x + ", " + bottomLeft.y);
                        System.out.println("BottomRight: " + bottomRight.x + ", " + bottomRight.y);
                    }
                    System.out.println(" ");

                    System.out.println("-- Irl Coords --");

                    for (Point ball : ballCoords) {
                        if (conversionFactor != 0)
                            System.out.println("ball: " + ball.x * conversionFactor + ", " + ball.y * conversionFactor);
                    }
                    System.out.println(" ");

                    if (irlTopLeft != null) { // They will all be not null at the same time
                        System.out.println("TopLeft: " + irlTopLeft.x + ", " + irlTopLeft.y);
                        System.out.println("TopRight: " + irlTopRight.x + ", " + irlTopRight.y);
                        System.out.println("BottomLeft: " + irlBottomLeft.x + ", " + irlBottomLeft.y);
                        System.out.println("BottomRight: " + irlBottomRight.x + ", " + irlBottomRight.y);
                    }
                    System.out.println(" ");
                }

                // Display frame
                HighGui.imshow("frame", frame); // Display frame
                HighGui.waitKey(1);

            } else {
                break;
            }
        }

        HighGui.destroyAllWindows();
        robotController.stopController();
    }

    private static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    private static double angleBetweenTwoPoints(double x1, double y1, double x2, double y2){
        double angle = Math.atan( (y2-y1) / (x2-x1)) * 180/Math.PI;

        // Add depending on quadrant
        if (x2 - x1 <= 0 && y2 - y1 >= 0) // Quadrant 2
            angle += 180;
        else if (x2 - x1 <= 0 && y2 - y1 <= 0) // Quadrant 3
            angle += 180;
        else if (x2 - x1 >= 0 && y2 - y1 <= 0) // Quadrant 4
            angle += 360;

        return angle;
    }

    private static Point findClosestBall(Point[] balls, Point centerMarker){
        if (balls.length == 0) return null;
        Point closestBall = balls[0];
        double closestDistance = distanceBetweenTwoPoints(closestBall.x, closestBall.y, centerMarker.x, centerMarker.y);

        // Find ball closest to centerMarker
        for (int i = 0; i < balls.length; i++) {
            Point ball = balls[i];
            double distance = distanceBetweenTwoPoints(ball.x, ball.y, centerMarker.x, centerMarker.y);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestBall = ball;
            }
        }

        return closestBall;
    }
}