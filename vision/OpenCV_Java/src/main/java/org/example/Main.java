package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Initialize library
        OpenCV.loadLocally();

        VideoCapture capture = new VideoCapture();

        capture.open(2); // Might need to be changed

        long start = System.currentTimeMillis();

        while (true) {
            if (capture.isOpened()) {
                Mat frame = new Mat();
                capture.read(frame);

                //Converting the image to Gray
                Mat frame_gray = new Mat();
                Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_RGBA2GRAY);

                Mat frame_blur = new Mat();
                Imgproc.medianBlur(frame_gray, frame_blur, 5);

                if (!frame.empty()) {
                    Mat circles = new Mat();
                    Imgproc.HoughCircles(frame_blur, circles, Imgproc.HOUGH_GRADIENT, 1, 30, 20, 17, 6, 8);

                    if (!circles.empty()) {
                        for (int i = 0; i < circles.width(); i++) {
                            double[] center = circles.get(0, i);
                            System.out.println(center[0] + ", " + center[1]);
                        }
                    }
                }
            }
        }
    }
}