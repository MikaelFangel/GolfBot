package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Initialize library
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture();

        capture.open(0); // Might need to be changed

        while (true) {
            if (capture.isOpened()) {
                ArrayList<double[]> circleCoords = getCirclesFromFrame(capture);

                for (double[] circle : circleCoords) {
                    System.out.println("X: " + circle[0] + ", Y: " + circle[1]);
                }
            }
        }
    }

    public static ArrayList<double[]> getCirclesFromFrame(VideoCapture capture) {
        Mat frame = new Mat();
        capture.read(frame);

        //Converting the image to Gray and blur it
        Mat frame_gray = new Mat();
        Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_RGBA2GRAY);
        Mat frame_blur = new Mat();
        Imgproc.medianBlur(frame_gray, frame_blur, 5);


        ArrayList<double[]> circleCoords = new ArrayList<>();

        if (!frame.empty()) {
            // Get circles from frame
            Mat circles = new Mat();
            Imgproc.HoughCircles(frame_blur, circles, Imgproc.HOUGH_GRADIENT, 1, 30, 20, 17, 6, 8);

            // Add circle coords to return arraylist
            if (!circles.empty()) {
                for (int i = 0; i < circles.width(); i++) {
                    double[] center = circles.get(0, i);

                    double[] coords = new double[2];
                    coords[0] = center[0];
                    coords[1] = center[1];

                    circleCoords.add(coords);
                }
            }
        }

        return circleCoords;
    }
}