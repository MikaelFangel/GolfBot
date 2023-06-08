package vision.detection;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import vision.helperClasses.BorderSet;
import vision.helperClasses.MaskSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BorderDetector implements SubDetector {
    private BorderSet borderSet;
    private final List<MaskSet> maskSets = new ArrayList<>();;

    /**
     * Detects the border from the frame and stores the objects in its own objects.
     * @param frame The frame to be detected.
     * @return a boolean symbolizing if objects were found or not.
     */
    public boolean detectBorder(Mat frame) {
        this.borderSet = getBorderFromFrame(frame);

        return borderSet != null;
    }

    /**
     * Finds the border and calculates the corners from an approximation of line intersections.
     * Note: The mask is displayed in black and white. White equals true
     * @param frame to be evaluated
     * @return A BorderSet with the border corners and the offset from the camera.
     */
    private  BorderSet getBorderFromFrame(Mat frame) {
        // Remove everything from frame except border (which is red)
        Scalar lower = new Scalar(0, 0, 180); // Little red
        Scalar upper = new Scalar(100, 100, 255); // More red

        // Create a mask to filter in next step
        Mat mask = new Mat();
        Core.inRange(frame, lower, upper, mask); // Filter all red colors from frame to mask

        // Filter out to only have the red border
        Mat frameBorder = new Mat();
        Core.bitwise_and(frame, frame, frameBorder, mask); // Overlay mask on frame and get the red border

        // Add mask for debugging
        this.maskSets.add(new MaskSet("border", mask));

        // Greyscale to allow finding contours and blur
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frameBorder, frameGray, Imgproc.COLOR_BGR2GRAY);

        // Blur frame to smooth out color inconsistencies
        Mat frameBlur = new Mat();
        Imgproc.GaussianBlur(frameGray, frameBlur, new Size(9, 9), 0);

        // Find contours (color patches of the border)
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(frameBlur, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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

            // Exit if the four lines are found. Because we only need to have 4.
            if (approx.toArray().length == 4) {
                lines = approx;
                break;
            }
        }

        // End if lines are not found
        if (lines.empty()) return null;

        // Get as array
        Point[] linePoints = lines.toArray();

        // Calculate corners
        Point[] corners = new Point[linePoints.length];
        for (int i = 0; i < corners.length; i++) {
            Point point = linePoints[i];
            corners[i] = new Point(point.x, point.y);
        }

        // Sort corners in order: {TopLeft, TopRight, BottomLeft, BottomRight}
        List<Point> sortedCorners = new ArrayList<>(Arrays.stream(corners).sorted((p1, p2) -> (int) ((p1.x + p1.y) - (p2.x + p2.y))).toList());
        if (sortedCorners.get(1).x < sortedCorners.get(2).x) {
            Point temp = sortedCorners.get(1);
            sortedCorners.set(1, sortedCorners.get(2));
            sortedCorners.set(2, temp);
        }

        // Get offset
        double offsetX = sortedCorners.get(0).x;
        double offsetY = sortedCorners.get(0).y;

        return new BorderSet(sortedCorners.toArray(Point[]::new), new Point(offsetX, offsetY));
    }

    public BorderSet getBorderSet() {
        return this.borderSet;
    }

    @Override
    public List<MaskSet> getMaskSets() {
        return this.maskSets;
    }
}
