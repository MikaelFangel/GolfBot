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
    // Blue markers threshold (BGR)
    private final Scalar lRobot = new Scalar(100, 100, 0);
    private final Scalar uRobot = new Scalar(255, 255, 20);

    // Size of contours (number of pixels in cohesive area)
    final int areaLowerThreshold = 100;
    final int areaUpperThreshold = 1000;


    private Robot robot;
    List<MaskSet> maskSets;

    public RobotDetector() {
        maskSets = new ArrayList<>();
    }

    public boolean detectRobot(Mat frame) {
        Point[] markers = getRobotMarkers(frame);

        if (markers != null) {
            Point center = markers[0];
            Point front = markers[1];

            // Calculate angle of the robot
            double robotAngle = angleBetweenTwoPoints(center.x, center.y, front.x, front.y);

            robot = new Robot(markers[0], markers[1], robotAngle);
        }

        return robot != null;
    }

    public Point[] getRobotMarkers(Mat frame) {
        // Blur frame to smooth out color inconsistencies
        Mat frameBlur = new Mat();
        Imgproc.GaussianBlur(frame, frameBlur, new Size(7,7), 7, 0);

        // Create a mask to filter out unnecessary contours
        Mat mask = new Mat();
        Core.inRange(frameBlur, lRobot, uRobot, mask);

        // Add mask for debugging
        maskSets.add(new MaskSet("robotMask", mask));

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
        if (contourSets.size() < 2) return null;

        // ! Find coords of markers !
        double[] centerCoords = {-1, -1}, directionCoords = {-1, -1};

        // Sort list to descending order.
        contourSets.sort(Comparator.comparingDouble(ContourSet::getArea));
        Collections.reverse(contourSets);

        for (int i = 0; i < 2; i++) { // Loop through 2 biggest contours
            MatOfPoint contour = contourSets.get(i).getContour();

            // Get bounding rectangle
            Rect rect = Imgproc.boundingRect(contour);

            // Get center of markers
            switch (i) {
                case 0 -> {
                    centerCoords[0] = rect.x + rect.width / 2.;
                    centerCoords[1] = rect.y + rect.height / 2.;
                }
                case 1 -> {
                    directionCoords[0] = rect.x + rect.width / 2.;
                    directionCoords[1] = rect.y + rect.height / 2.;
                }
            }
        }

        // Convert to Point[]
        Point[] coords = new Point[2];
        coords[0] = new Point(centerCoords);
        coords[1] = new Point(directionCoords);

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
