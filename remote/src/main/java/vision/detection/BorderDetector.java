package vision.detection;

import courseObjects.Border;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import vision.helperClasses.MaskSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BorderDetector implements SubDetector {
    private Border border;
    private Point cameraOffset;
    private final List<MaskSet> maskSets = new ArrayList<>();;

    // Initialize all OpenCV objects once to not have memory leaks
    Mat mask, frameBorder, frameGray, frameBlur, frameDummy;
    MatOfPoint2f lines, approx;
    private boolean initial = true;

    /**
     * Detects the border from the frame and stores the objects in its own objects.
     * @param frame The frame to be detected.
     * @return a boolean symbolizing if objects were found or not.
     */
    public boolean detectBorder(Mat frame) {
        this.border = getBorderFromFrame(frame);
        // Initialize all OpenCV objects once to not have memory leaks
        if (initial) {
            mask = new Mat();
            frameBorder = new Mat();
            frameGray = new Mat();
            frameBlur = new Mat();
            frameDummy = new Mat();
            lines = new MatOfPoint2f();
            approx = new MatOfPoint2f();

            initial = false;
        }

        this.borderSet = getBorderFromFrame(frame);

        return border != null;
    }

    /**
     * Finds the border and calculates the corners from an approximation of line intersections.
     * Note: The mask is displayed in black and white. White equals true
     * @param frame to be evaluated
     * @return A BorderSet with the border corners and the offset from the camera.
     */
    private Border getBorderFromFrame(Mat frame) {
        // Remove everything from frame except border (which is red)
        Scalar lower = new Scalar(0, 0, 180); // Little red
        Scalar upper = new Scalar(100, 100, 255); // More red

        // Create a mask to filter in next step
        Core.inRange(frame, lower, upper, mask); // Filter all red colors from frame to mask

        // Filter out to only have the red border
        Core.bitwise_and(frame, frame, frameBorder, mask); // Overlay mask on frame and get the red border

        // Add mask for debugging
        this.maskSets.add(new MaskSet("border", mask));

        // Greyscale to allow finding contours and blur
        Imgproc.cvtColor(frameBorder, frameGray, Imgproc.COLOR_BGR2GRAY);

        // Blur frame to smooth out color inconsistencies
        Imgproc.GaussianBlur(frameGray, frameBlur, new Size(9, 9), 0);

        // Find contours (color patches of the border)
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(frameBlur, contours, frameDummy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Estimate lines for the border using the contours
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contourConverted = new MatOfPoint2f(contour.toArray());

            // Approximate polygon of contour
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
        cameraOffset = new Point(offsetX, offsetY);

        return new Border(sortedCorners.get(0), sortedCorners.get(1), sortedCorners.get(2), sortedCorners.get(3));
    }

    public Border getBorder() {
        return this.border;
    }
    public Point getCameraOffset() {
        return this.cameraOffset;
    }

    @Override
    public List<MaskSet> getMaskSets() {
        return this.maskSets;
    }
}
