package vision.detection;

import courseObjects.Border;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import vision.helperClasses.BorderSet;
import vision.helperClasses.MaskSet;

import java.util.ArrayList;
import java.util.List;

public class BorderDetector implements SubDetector {

    // Red color thresholds
    Scalar lower = new Scalar(0, 150, 150);
    Scalar upper = new Scalar(10, 255, 255);

    private BorderSet borderSet;
    List<MaskSet> maskSets;

    public BorderDetector() {
        maskSets = new ArrayList<>();
    }

    public void detectBorder(Mat frame) {
        borderSet = getBorderFromFrame(frame);
    }

    /**
     * Returns the coordinates of the border of the course.
     * @param frame to be evaluated
     * @return null if there are not found exactly 4 lines, else the 4 coordinates of the border intersections.
     */
    private  BorderSet getBorderFromFrame(Mat frame) {
        Mat frameHSV = new Mat();
        Mat maskRed = new Mat();
        Mat frameCourse = new Mat();
        Mat frameGray = new Mat();
        Mat frameBlur = new Mat();

        // Convert to HSV color format
        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);

        // Remove everything from frame except border (which is red)
        Core.inRange(frameHSV, lower, upper, maskRed);
        Core.bitwise_and(frame, frame, frameCourse, maskRed);

        // Add mask for debugging
        maskSets.add(new MaskSet("border", maskRed));

        // Greyscale and blur
        Imgproc.cvtColor(frameCourse, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(frameGray, frameBlur, new Size(9, 9), 0);

        // Find contours (color patches of the border
        List<MatOfPoint> contours = new ArrayList<>();
        Mat dummyHierarchy = new Mat();
        Imgproc.findContours(frameBlur, contours, dummyHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Estimate lines for the border using the contours
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

            // Exit if the four lines are found. Becuase we only need to have 4.
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

            corners[i] = new Point(point.x, point.y);
        }

        return new BorderSet(corners, new Point(offsetX, offsetY));
    }

    public BorderSet getBorderSet() {
        return borderSet;
    }

    @Override
    public List<MaskSet> getMaskSets() {
        return maskSets;
    }
}
