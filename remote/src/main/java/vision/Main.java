package vision;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import static vision.Detection.*;

public class Main {
    public static void main(String[] args) {
        int realWidth = 167;
        int realHeight = 122;

        // Initialize library
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture();
        capture.open(0); // Might need to be changed
        // capture.open("/home/frederik/Desktop/border.mp4");

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

                Point[] cornerCoords = getBorderFromFrame(frame);

                double conversionFactor = 0;
                if (cornerCoords != null) {
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

                // Pure debugging
                System.out.println("\n### New Frame ###");

                System.out.println("-- Pixel Coords --");
                for (Point ball: ballCoords) {
                    System.out.println("Circle: " + ball.x + ", " + ball.y);
                }

                System.out.println(" ");
                for (Point marker: robotMarkerCoords) {
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

                for (Point ball: ballCoords) {
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
            } else {
                break;
            }
        }
    }

    private static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }
}