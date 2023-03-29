package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import java.util.ArrayList;

import static org.example.Detection.*;

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

                Point[] circleCoords = getCircleCoordsFromFrame(frame);
                Point[] rotationCoords = getRotationCoordsFromFrame(frame);

                // Get border coords
                Point[] cornerCoords = getBorderFromFrame(frame);
                if (cornerCoords != null) {
                    Point topLeft = new Point(cornerCoords[0].x, cornerCoords[0].y);
                    Point topRight = new Point(cornerCoords[1].x, cornerCoords[1].y);
                    Point bottomRight = new Point(cornerCoords[2].x, cornerCoords[2].y);
                    Point bottomLeft = new Point(cornerCoords[3].x, cornerCoords[3].y);

                    // Get irl coordinates
                    double conversionFactor = realWidth / distanceBetweenTwoPoints(topLeft.x, topLeft.y, topRight.x, topRight.y);

                    Point irlTopLeft = new Point(cornerCoords[0].x * conversionFactor, cornerCoords[0].y * conversionFactor);
                    Point irlTopRight = new Point(cornerCoords[1].x * conversionFactor, cornerCoords[1].y * conversionFactor);
                    Point irlBottomRight = new Point(cornerCoords[2].x * conversionFactor, cornerCoords[2].y * conversionFactor);
                    Point irlBottomLeft = new Point(cornerCoords[3].x * conversionFactor, cornerCoords[3].y * conversionFactor);
                }
            } else {
                break;
            }
        }
    }

    private static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }
}