package org.example;

import org.example.helper.ContourSet;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Detection {
    public static ArrayList<double[]> getCirclesFromFrame(Mat frame) {
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

    /**
     * @param frame
     * @return List(centerCoords, directionCoords)
     */
    public static ArrayList<double[]> getRotationCoordsFromFrame(Mat frame) {
        final int area_lower = 60;
        final int area_upper = 350;

        // Convert frame to HSV
        Mat hsv_frame = new Mat();
        Imgproc.cvtColor(frame, hsv_frame, Imgproc.COLOR_BGR2HSV);

        // Blur the frame
        Mat blur_frame = new Mat();
        Imgproc.GaussianBlur(frame, blur_frame, new Size(7,7), 7, 0);

        // Create a mask
        Mat mask = new Mat();
        Scalar lower = new Scalar(100, 100, 50);
        Scalar upper = new Scalar(255, 255, 255);
        Core.inRange(blur_frame, lower, upper, mask);

        // Get Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat dummy_hierarchy = new Mat();
        Imgproc.findContours(mask, contours, dummy_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Get useful contour areas
        ArrayList<ContourSet> newList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area >= area_lower && area <= area_upper) {
                newList.add(new ContourSet(area, contour));
            }
        }

        // Exit if there are not two coordinates
        if (newList.size() < 2) return null;

        // ! Find coords of markers !
        double[] centerCoords = {-1, -1}, directionCoords = {-1, -1};

        // Sort list to biggest first
        // TODO newList.sort();

        for (int i = 0; i < 2; i++) { // Loop through 2 biggest contours
            MatOfPoint contour = newList.get(i).contour;

            // Get bounding rectangle
            Rect rect = Imgproc.boundingRect(contour);

            // Get center of rectangles
            if (i == 0) { // For center marker
                centerCoords[0] = rect.x + rect.width / 2;
                centerCoords[1] = rect.y + rect.height / 2;
            }
            if (i == 1) { // For direction marker
                directionCoords[0] = rect.x + rect.width / 2;
                directionCoords[1] = rect.y + rect.height / 2;
            }
        }

        ArrayList<double[]> coords = new ArrayList<>();
        coords.add(centerCoords);
        coords.add(directionCoords);

        return coords;
    }
}
