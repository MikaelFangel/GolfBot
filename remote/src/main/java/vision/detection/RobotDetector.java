package vision.detection;

import courseObjects.Robot;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import vision.helperClasses.ContourSet;
import vision.helperClasses.MaskSet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static vision.math.Geometry.angleBetweenTwoPoints;

public class RobotDetector implements SubDetector {
    private Robot robot;
    List<MaskSet> maskSets = new ArrayList<>();

    private final int numberOfMarkers = 2;

    // Initialize all OpenCV objects once to not have memory leaks
    Mat frameBlur, mask, frameDummy;
    private boolean initial = true;

    /**
     * Detects the robot from the frame and stores it in the objects
     * @param frame The frame to be evaluated.
     * @return a boolean symbolizing if the robot was found or not.
     */
    public boolean detectRobot(Mat frame) {
        // Initialize all OpenCV objects once to not have memory leaks
        if (initial) {
            frameBlur = new Mat();
            mask = new Mat();
            frameDummy = new Mat();

            initial = false;
        }

        Point[] markers = getRobotMarkers(frame);

        // Store robot if robot markers were found
        if (markers != null) {
            Point center = markers[0]; // Big marker
            Point front = markers[1]; // Small marker

            // Calculate angle of the robot
            double robotAngle = angleBetweenTwoPoints(center.x, center.y, front.x, front.y);

            this.robot = new Robot(markers[0], markers[1], robotAngle);
        }

        return this.robot != null;
    }

    /**
     * Finds the coordinates of the robots marker from the frame
     * @param frame The frame to be evaluated
     * @return Return a Point array of length always 2, or null if not found
     */
    public Point[] getRobotMarkers(Mat frame) {
        // Blur frame to smooth out color inconsistencies
        Imgproc.GaussianBlur(frame, frameBlur, new Size(7,7), 7, 0);

        // Blue markers threshold (BGR)
        final Scalar lRobot = new Scalar(100, 100, 0);
        final Scalar uRobot = new Scalar(255, 255, 20);

        // Create a mask to filter out unnecessary contours
        Core.inRange(frameBlur, lRobot, uRobot, mask);

        // Add mask for debugging
        maskSets.add(new MaskSet("robotMask", mask));

        // Get Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, frameDummy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Size of contours (number of pixels in cohesive area)
        final int areaLowerThreshold = 100;
        final int areaUpperThreshold = 1000;

        // Get useful contour areas
        ArrayList<ContourSet> contourSets = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area >= areaLowerThreshold && area <= areaUpperThreshold) {
                contourSets.add(new ContourSet(area, contour));
            }
        }

        // Exit if there are less than two coordinates
        if (contourSets.size() < numberOfMarkers) return null;

        // ! Find coords of markers !
        double[] bigMarkerCoords = new double[2], smallMarkerCoords = new double[2];

        // Sort list in descending order.
        contourSets.sort(Comparator.comparingDouble(ContourSet::getArea));
        Collections.reverse(contourSets);

        for (int i = 0; i < numberOfMarkers; i++) { // Loop through 2 biggest contours
            MatOfPoint contour = contourSets.get(i).getContour();

            // Get bounding rectangle
            Rect rect = Imgproc.boundingRect(contour);

            // Get center of markers
            switch (i) {
                case 0 -> { // Big marker
                    bigMarkerCoords[0] = rect.x + rect.width / 2.;
                    bigMarkerCoords[1] = rect.y + rect.height / 2.;
                }
                case 1 -> { // Small marker
                    smallMarkerCoords[0] = rect.x + rect.width / 2.;
                    smallMarkerCoords[1] = rect.y + rect.height / 2.;
                }
            }
        }

        // Convert to Point[]
        Point[] coords = new Point[numberOfMarkers];
        coords[0] = new Point(bigMarkerCoords);
        coords[1] = new Point(smallMarkerCoords);

        return coords;
    }

    public Robot getRobot() {
        return robot;
    }

    @Override
    public List<MaskSet> getMaskSets() {
        return maskSets;
    }
}
