package vision;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import vision.helperClasses.BorderSet;
import vision.helperClasses.ContourSet;

import java.util.*;

public class Detection {
    /**
     * Returns a Point array of center coordinates for each circle found on the board.
     * @param frame to be evaluated.
     * @return Point array, with coordinates of the center for each circle.
     */
    public static Point[] getCircleCoordsFromFrame(Mat frame) {
        //Converting the image to Gray and blur it
        Mat frameGray = new Mat();
        Mat frameBlur = new Mat();

        Imgproc.GaussianBlur(frame, frameBlur, new Size(9,9), 0);
        Imgproc.cvtColor(frameBlur, frameGray, Imgproc.COLOR_BGR2GRAY);

        ArrayList<double[]> circleCoords = new ArrayList<>();

        if (!frame.empty()) {
            // Get circles from frame
            Mat circles = new Mat();
            Imgproc.HoughCircles(frameGray, circles, Imgproc.HOUGH_GRADIENT, 1, 50, 25, 17, 4, 8);

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

        // Convert to Point[]
        Point[] coords = new Point[circleCoords.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = new Point(circleCoords.get(i));
        }

        return coords;
    }

    /**
     * Returns and 2 long list of coordinates. First coordinates for the biggest marker
     * the second for the next biggest one. Both on the robot.
     * The markers have to be blue.
     * @param frame that needs to be evaluated.
     * @return Returns array of points for the 2 markers.
     */
    public static Point[] getRotationCoordsFromFrame(Mat frame) {
        final int areaLowerThreshold = 60;
        final int areaUpperThreshold = 350;

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
        ArrayList<ContourSet> contourSets = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area >= areaLowerThreshold && area <= areaUpperThreshold) {
                contourSets.add(new ContourSet(area, contour));
            }
        }

        // Exit if there are not two coordinates
        if (contourSets.size() < 2) return new Point[]{};

        // ! Find coords of markers !
        double[] centerCoords = {-1, -1}, directionCoords = {-1, -1};

        // Sort list to descending order.
        contourSets.sort(Comparator.comparingDouble(set -> set.area));
        Collections.reverse(contourSets);

        for (int i = 0; i < 2; i++) { // Loop through 2 biggest contours
            MatOfPoint contour = contourSets.get(i).contour;

            // Get bounding rectangle
            Rect rect = Imgproc.boundingRect(contour);

            // Get center of rectangles
            if (i == 0) { // For center marker
                centerCoords[0] = rect.x + rect.width / 2.;
                centerCoords[1] = rect.y + rect.height / 2.;
            }
            else if (i == 1) { // For direction marker
                directionCoords[0] = rect.x + rect.width / 2.;
                directionCoords[1] = rect.y + rect.height / 2.;
            }
        }

        // Convert to Point[]
        Point[] coords = new Point[2];
        coords[0] = new Point(centerCoords);
        coords[1] = new Point(directionCoords);

        return coords;
    }

    /**
     * Returns the coordinates of the border of the course.
     * @param frame to be evaluated
     * @return null if there are not found exactly 4 lines, else the 4 coordinates of the border intersections.
     */
    public static BorderSet getBorderFromFrame(Mat frame) {
        Mat frameHSV = new Mat();
        Mat maskRed = new Mat();
        Mat frameCourse = new Mat();
        Mat frameGray = new Mat();
        Mat frameBlur = new Mat();

        // Convert to HSV and
        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);

        // Remove everything from frame except border (which is red)
        Scalar lower = new Scalar(0, 150, 150);
        Scalar upper = new Scalar(10, 255, 255);
        Core.inRange(frameHSV, lower, upper, maskRed);
        Core.bitwise_and(frame, frame, frameCourse, maskRed);

        // Greyscale and blur
        Imgproc.cvtColor(frameCourse, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(frameGray, frameBlur, new Size(9, 9), 0);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat dummyHierarchy = new Mat();
        Imgproc.findContours(frameBlur, contours, dummyHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find border from contours
        MatOfPoint2f lines = new MatOfPoint2f();
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contourConverted = new MatOfPoint2f(contour.toArray());

            // Approximate polygon of contour
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(
                    contourConverted,
                    approx,
                    0.01 * Imgproc.arcLength(contourConverted, true),
                    true

            );

            // Exit if the four lines are found.
            if (approx.toArray().length == 4) {
                lines = approx;
                break;
            }
        }

        // End if lines are not found
        if (lines.empty()) return null;

        // Get as array
        Point[] linePoints = lines.toArray();

        // Get offset
        double offsetX = linePoints[0].x;
        double offsetY = linePoints[0].y;

        // Calculate corners
        Point[] corners = new Point[linePoints.length];
        for (int i = 0; i < corners.length; i++) {
            Point point = linePoints[i];

            corners[i] = new Point(point.x - offsetX, point.y - offsetY);
        }

        BorderSet borderSet = new BorderSet(corners, new Point(offsetX, offsetY));

        return borderSet;
    }
}
