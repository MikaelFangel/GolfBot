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

                // ArrayList<double[]> circleCoords = getCirclesFromFrame(frame);
                // ArrayList<double[]> rotationCoords = getRotationCoordsFromFrame(frame);

                // Get border coords
                Point[] cornerCoords = getBorderFromFrame(frame);
                if (cornerCoords != null) {
                    Point topLeft = new Point(cornerCoords[0].x, cornerCoords[0].y);
                    Point topRight = new Point(cornerCoords[1].x, cornerCoords[1].y);
                    Point bottomRight = new Point(cornerCoords[2].x, cornerCoords[2].y);
                    Point bottomLeft = new Point(cornerCoords[3].x, cornerCoords[3].y);

                    // conversionFactor = realWidth /
                }
            } else {
                break;
            }
        }
    }
}