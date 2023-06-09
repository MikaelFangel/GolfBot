package vision.detection;

import courseObjects.Border;
import courseObjects.Cross;
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
    private final Cross cross = new Cross();

    // Initialize all OpenCV objects once to not have memory leaks (so they don't get reinitialized every time the function gets called)
    Mat mask, frameBlur, frameDummy;
    MatOfPoint2f innerBorderEndPoints;
    MatOfPoint2f approx;
    private boolean initial = true;

    /**
     * Detects the border from the frame and stores the objects in its own objects.
     * @param frame The frame to be detected.
     * @return a boolean symbolizing if objects were found or not.
     */
    public boolean detectBorder(Mat frame) {
        // Initialize all OpenCV objects once to not have memory leaks
        if (initial) {
            mask = new Mat();
            frameBlur = new Mat();
            frameDummy = new Mat();

            initial = false;
        }

        this.border = getBorderFromFrame(frame);

        return border != null;
    }

    /**
     * Finds the border and calculates the corners from an approximation of line intersections.
     * Note: The mask is displayed in black and white. White equals true
     * @param frame to be evaluated
     * @return A BorderSet with the border corners and the offset from the camera.
     */
    private Border getBorderFromFrame(Mat frame) {
        List<MatOfPoint> contours = getRedContours(frame);
        /* Each contour is a boundary of one of the components (e.g. to boundary of the cross)
         *  - The outer boundary of the border has 28 straight lines in the physical world (not used)
         *  - The inner boundary of the border has 4 straight lines in the physical world
         *  - The boundary of the cross has 12 straight lines in the physical worlds
         *
         * NB! The lines from the physical world might differ a bit from what is found in contours */
        List<MatOfPoint2f> endPointList = new ArrayList<>();
        boolean crossFound = false;

        int innerBorderIndex = contours.size() - 2;
        for (int i = innerBorderIndex; i >= 0; i--) { // The last element would be the outer boundary of the border
            MatOfPoint2f contourConverted = new MatOfPoint2f(contours.get(i).toArray());
            approx = new MatOfPoint2f();

            // Approximate polygon of contour
            Imgproc.approxPolyDP(
                    contourConverted,
                    approx,
                    // The value 0.014 has been tweaked to get the cross coordinates as reliably as possible
                    0.014 * Imgproc.arcLength(contourConverted, true),
                    true
            );

            int numOfEndPoints = approx.toArray().length;

            if (i == innerBorderIndex && numOfEndPoints == 4) { // The boundary of inner border
                innerBorderEndPoints = approx;
            } else { // Obstacles with same color as border
                if (numOfEndPoints == 12) { // Objects with 12 end points, e.g. a cross
                    crossFound = true;
                    endPointList.add(approx);
                }
            }
        }
        if (innerBorderEndPoints == null || innerBorderEndPoints.empty()) return null;

        if (crossFound)
            updateCross(endPointList);

        // Add inner boundary end points of border to BorderSet object
        Point[] linePoints = innerBorderEndPoints.toArray().clone();
        approx.release(); // Prevent memory leak. Used in the endPointList

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

    /**
     * The red color filtered from the frame using a mask with a color threshold. The frame is then scaled grey to blur
     * it and find contours. The frame is blurred to smooth out color inconsistencies, and
     *
     * @param frame Frame to get contours from
     * @return The red contours of the frame, one for each red 'object'.
     */
    private List<MatOfPoint> getRedContours(Mat frame) {
        // Remove everything from frame except border (which is red)
        Scalar lower = new Scalar(0, 0, 160); // Little red
        Scalar upper = new Scalar(100, 100, 255); // More red

        // Blur frame to smooth out color inconsistencies
        Imgproc.GaussianBlur(
                frame,
                this.frameBlur,
                /* Both width and height should be uneven numbers.
                 *  - Should be at least (3, 3) to detect borders.
                 *  - Points of the cross becomes shaky if larger than (3, 3) */
                new Size(3, 3),
                0
        );

        // Create a mask to filter in next step
        Core.inRange(this.frameBlur, lower, upper, this.mask); // Filter all red colors from frame to mask

        // Add mask for debugging
        this.maskSets.add(new MaskSet("border", this.mask));

        // Find contours (color patches of the border)
        List<MatOfPoint> contours = new ArrayList<>();
        int method = Imgproc.CHAIN_APPROX_SIMPLE; // Only leaves the end points of the components, e.g. a rectangular contour would be encoded with 4 points.
        Imgproc.findContours(this.mask, contours, frameDummy, Imgproc.RETR_LIST, method);

        return contours;
    }

    /**
     * Update the variables in the Cross object with the newly detected endpoints
     *
     * @param endPointList List of 12 endpoints on the cross
     */
    private void updateCross(List<MatOfPoint2f> endPointList) {
        // Add end points of cross to Cross object. Used for debugging
        List<Point> endPoints = new ArrayList<>();
        for (MatOfPoint2f endPoint : endPointList) {
            endPoints.addAll(endPoint.toList());
            cross.setEndPoints(endPoints);
        }

        Point firstPoint = endPoints.get(0);
        Point rightFromFirst = endPoints.get(endPoints.size() - 1);
        Point leftFromFirst = endPoints.get(1);

        // Calculate distance from first point to next point on both sides
        double lengthRight = Math.sqrt(Math.pow((rightFromFirst.x - firstPoint.x), 2) + Math.pow((rightFromFirst.y - firstPoint.y), 2));
        double lengthLeft = Math.sqrt(Math.pow((leftFromFirst.x - firstPoint.x), 2) + Math.pow((leftFromFirst.y - firstPoint.y), 2));

        if (lengthRight > lengthLeft) {
            this.cross.setMeasurePoint(new Point((firstPoint.x + leftFromFirst.x) / 2, (firstPoint.y + leftFromFirst.y) / 2));
        } else {
            this.cross.setMeasurePoint(new Point((firstPoint.x + rightFromFirst.x) / 2, (firstPoint.y + rightFromFirst.y) / 2));
        }

        // Add middle coordinate to Cross object
        Point middlePoint = endPoints.get(6);
        this.cross.setMiddle(new Point((firstPoint.x + middlePoint.x) / 2, (firstPoint.y + middlePoint.y) / 2));
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

    public Cross getCross() {
        return this.cross;
    }
}
