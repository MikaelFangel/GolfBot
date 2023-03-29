package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import java.util.ArrayList;

import static org.example.Detection.*;

public class Main {
    public static void main(String[] args) {
        // Initialize library
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture();
        capture.open(2); // Might need to be changed

        // Main Loop
        while (true) {
            if (capture.isOpened()) {
                Mat frame = new Mat();
                capture.read(frame);

                // ArrayList<double[]> circleCoords = getCirclesFromFrame(frame);
                ArrayList<double[]> rotationCoords = getRotationCoordsFromFrame(frame);

                if (rotationCoords != null) {
                    for (double[] circle : rotationCoords) {
                        System.out.println("X: " + circle[0] + ", Y: " + circle[1]);
                    }
                }
            }
        }
    }
}