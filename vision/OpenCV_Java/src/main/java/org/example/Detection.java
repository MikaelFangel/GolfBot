package org.example;

import org.example.helperClasses.ContourSet;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class Detection {
    /**
     * Returns a list of coordinates for each circle found on the board
     * @param frame
     * @return List({x, y}, {x, y} ...)
     */
    public static ArrayList<double[]> getCircleCoordsFromFrame(Mat frame) {
        //Converting the image to Gray and blur it
        Mat frameGray = new Mat();
        Mat frameBlur = new Mat();

        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(frameGray, frameBlur, 5);

        ArrayList<double[]> circleCoords = new ArrayList<>();

        if (!frame.empty()) {
            // Get circles from frame
            Mat circles = new Mat();
            Imgproc.HoughCircles(frameBlur, circles, Imgproc.HOUGH_GRADIENT, 1, 50, 25, 17, 4, 8);

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
     * Returns and 2 long list of coordinates. First coordinates for the biggest marker
     * the second for the next biggest one.
     * @param frame
     * @return List(centerCoords, directionCoords)
     */
    public static ArrayList<double[]> getRotationCoordsFromFrame(Mat frame) {
        final int areaLower = 60;
        final int areaUpper = 350;

        // Transform frame
        Mat frameHSV = new Mat();
        Mat frameBlur = new Mat();

        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);
        Imgproc.GaussianBlur(frame, frameBlur, new Size(7,7), 7, 0);

        // Create a mask
        Mat mask = new Mat();
        Scalar lower = new Scalar(100, 100, 50);
        Scalar upper = new Scalar(255, 255, 255);
        Core.inRange(frameBlur, lower, upper, mask);

        // Get Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat dummyHierarchy = new Mat();
        Imgproc.findContours(mask, contours, dummyHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Get useful contour areas
        ArrayList<ContourSet> newList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area >= areaLower && area <= areaUpper) {
                newList.add(new ContourSet(area, contour));
            }
        }

        // Exit if there are not two coordinates
        if (newList.size() < 2) return null;

        // ! Find coords of markers !
        double[] centerCoords = {-1, -1}, directionCoords = {-1, -1};

        // Sort list to biggest first
        newList.sort(Comparator.comparingDouble(set -> set.area));
        Collections.reverse(newList);

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

    public static void getBorderFromFrame(Mat frame) {
        Mat frameHSV = new Mat();
        Mat maskRed = new Mat();
        Mat frameCourse = new Mat();
        Mat frameGray = new Mat();
        Mat frameBlur = new Mat();

        // Convert to HSV and
        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);

        // Remove every thing from frame except border (which is red)
        Scalar lower = new Scalar(0, 150, 150);
        Scalar upper = new Scalar(10, 255, 255);
        Core.inRange(frameHSV, lower, upper,maskRed);

        // Greyscale and blur
        Imgproc.cvtColor(frameCourse, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(frameGray, frameBlur, new Size(9, 9), 0);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat dummyHierarchy = new Mat();
        Imgproc.findContours(frameBlur, contours, dummyHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            MatOfPoint2f contourConverted = new MatOfPoint2f(contour.toArray());

            // Approximate polygon of contour
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP( // TODO this might be wrong
                    contourConverted,
                    approx,
                    0.01 * Imgproc.arcLength(contourConverted, true),
                    true

            );

            // Make sure polygon has 4 vertices
            if (approx.toArray().length == 4) {
                System.out.println(approx.toArray()[0]);
            }
        }
    }
}
