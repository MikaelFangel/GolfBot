package vision;

import courseObjects.Ball;
import courseObjects.Course;
import courseObjects.Robot;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import vision.helperClasses.BorderSet;
import vision.helperClasses.ContourSet;

import java.awt.*;
import java.util.*;
import java.util.List;

import static vision.math.Geometry.angleBetweenTwoPoints;
import static vision.math.Geometry.distanceBetweenTwoPoints;

public class Detection {

    private final Course course;
    private double conversionFactor;
    private Point originCameraOffset;

    private final Thread backgroundThread;

    // HoughCircles parameters. These configurations works okay with the current course setup
    private final int dp = 1;
    private final int minDist = 5; // Minimum distance between balls
    private final int param1 = 20;  // gradient value used in the edge detection
    private final int param2 = 12;  // lower values allow more circles to be detected (false positives)
    private final int minRadius = 1;  // limits the smallest circle to this size (via radius)
    private final int maxRadius = 8;  // similarly sets the limit for the largest circles


    // Color thresholds
    private Scalar lRobot = new Scalar(100, 100, 0);
    private Scalar uRobot = new Scalar(255, 255, 20);

    private int lWhiteBall = 210;
    private int uWhiteBall = 255;

    public Detection(int cameraIndex) {
        course = new Course();

        // Setup video capture
        OpenCV.loadLocally();
        VideoCapture capture = new VideoCapture();
        capture.open(cameraIndex);
        if (!capture.isOpened()) throw new RuntimeException("Camera Capture was not opened");

        initializeCourse(capture);

        // Spawn background thread;
        backgroundThread = new Thread(() -> detectCourse(capture));
        backgroundThread.start();

        System.out.println("Spawned Background Detection Thread");
    }

    private void initializeCourse(VideoCapture capture) {
        boolean courseFound = false, robotFound = false, ballsFound = false;

        // Fill course with variables
        System.out.println("Searching for course objects...");
        while (true) {
            Mat frame = new Mat(), debugFrame;
            capture.read(frame);
            debugFrame = frame;

            if (frame.empty()) throw new RuntimeException("Empty frame");

            HighGui.imshow("startFrame", debugFrame);
            HighGui.waitKey(1);

            // 1. Find course corner to establish conversion factor
            if (!courseFound) {
                courseFound = findCourseCorners(frame);
                if (!courseFound) continue;
                System.out.println("Found Course Corners");
            }

            // 2. Find Robot position and rotation
            if (!robotFound) {
                robotFound = findRobot(frame);
                if (!robotFound) continue;
                System.out.println("Found Robot");
            }

            // 3. Find balls on the course.
            if (!ballsFound) {
                ballsFound = findBalls(frame); // Parsed twice for debugging
                if (!ballsFound) continue;
                System.out.println("Found " + course.getBalls().size() + " balls");
                course.getBalls().forEach(ball -> System.out.println(ball.getCenter().x));
            }

            break;
        }

        HighGui.destroyWindow("startFrame");
    }

    private void detectCourse(VideoCapture capture) {
        while (true) {
            Mat frame = new Mat(), debugFrame;
            capture.read(frame);
            debugFrame = frame;

            findCourseCorners(frame);
            findRobot(frame);
            findBalls(frame);

            debugGUI(debugFrame);

            HighGui.imshow("frame", debugFrame);
            HighGui.waitKey(1);
        }
    }

    private boolean findCourseCorners(Mat frame) {
        Point topLeft, topRight, bottomRight, bottomLeft;
        Point irlTopLeft, irlTopRight, irlBottomLeft, irlBottomRight;

        // Try to get border set
        BorderSet borderSet = getBorderFromFrame(frame);
        if (borderSet == null) return false;

        // Get camera coordinates
        Point[] cornerCoords = borderSet.getCorrectCoords();


        topLeft = new Point(cornerCoords[0].x, cornerCoords[0].y);
        topRight = new Point(cornerCoords[1].x, cornerCoords[1].y);
        bottomRight = new Point(cornerCoords[2].x, cornerCoords[2].y);
        bottomLeft = new Point(cornerCoords[3].x, cornerCoords[3].y);

        // Calculate conversion factor and save origin offset for
        conversionFactor = course.length / distanceBetweenTwoPoints(topLeft.x, topLeft.y, topRight.x, topRight.y);
        originCameraOffset = borderSet.getOrigin();

        // Get irl coordinates
        irlTopLeft = pixelToCentimeter(topLeft);
        irlTopRight = pixelToCentimeter(topRight);
        irlBottomRight = pixelToCentimeter(bottomRight);
        irlBottomLeft = pixelToCentimeter(bottomLeft);

        // Push variables to course class
        course.setTopLeft(irlTopLeft);
        course.setTopRight(irlTopRight);
        course.setBottomLeft(irlBottomLeft);
        course.setBottomRight(irlBottomRight);

        return true;
    }

    private boolean findRobot(Mat frame) {
        Point[] robotMarkerCoords = getRotationCoordsFromFrame(frame);
        if (robotMarkerCoords.length < 2) return false;

        // Get robots two markers
        Point centerMarker = pixelToCentimeter(robotMarkerCoords[0]);
        Point rotationMarker = pixelToCentimeter(robotMarkerCoords[1]);

        // Calculate angle of the robot
        double robotAngle = angleBetweenTwoPoints(centerMarker.x, centerMarker.y, rotationMarker.x, rotationMarker.y);

        // Save variable to course object
        course.setRobot(new Robot(centerMarker, rotationMarker, robotAngle));

        return true;
    }


    /**
     * Detects white and orange balls on the course and updates position of balls in the course object.
     * @param frame that needs to be evaluated.
     * @return Returns true if any ball is found, else false
     */
    private boolean findBalls(Mat frame) {
        ArrayList<Ball> balls = new ArrayList<>();

        // Find the orange ball
        //Optional<Ball> orangeBall = findOrangeBall(frame);
        //orangeBall.ifPresent(balls::add);

        // Apply gray frame for detecting white balls
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        // Apply a binary threshold mask to seperate out all colors than white.
        Mat binaryFrame = new Mat();
        Imgproc.threshold(frameGray, binaryFrame, lWhiteBall, uWhiteBall, Imgproc.THRESH_BINARY);

        HighGui.imshow("ballMask", binaryFrame);

        // Apply blur for better noise reduction
        Mat frameBlur = new Mat();
        Imgproc.GaussianBlur(binaryFrame, frameBlur, new Size(7,7), 0);

        // Get white balls from frame
        Mat whiteballs = new Mat();
        Imgproc.HoughCircles(frameBlur, whiteballs, Imgproc.HOUGH_GRADIENT, dp, minDist, param1, param2, minRadius, maxRadius);

        if (!whiteballs.empty()) {
            // Add detected whiteballs to balls arraylist
            for (int i = 0; i < whiteballs.width(); i++) {
                double[] center = whiteballs.get(0, i);
                // Create the irl coordinates and create the ball object with the Color white
                Point coordinates = new Point((center[0] - originCameraOffset.x) * conversionFactor, (center[1] - originCameraOffset.y) * conversionFactor);
                balls.add(new Ball(coordinates, Color.WHITE));
            }
        }

        if (balls.size() == 0) return false;

        // Update ball positions
        course.setBalls(balls);
        return true;
    }

    /**
     * Detects and returns the orange ball on the course if it is found.
     * @param frame that needs to be evaluated.
     * @return Returns Optional<Ball>
     */
    private Optional<Ball> findOrangeBall(Mat frame) {
        // Apply hsv filter to distinguish orange ball
        Mat frameHsv = new Mat();
        Imgproc.cvtColor(frame, frameHsv, Imgproc.COLOR_BGR2HSV);

        // Create a mask to seperate the orange ball
        Mat mask = new Mat();
        Scalar lower = new Scalar(11, 50, 220);
        Scalar upper = new Scalar(30, 240, 255);
        Core.inRange(frameHsv, lower, upper, mask);

        // Apply blur for noise reduction
        Mat frameBlur = new Mat();
        Imgproc.GaussianBlur(mask, frameBlur, new Size(9,9), 0);

        // Stores orange circles from the HoughCircles algorithm
        Mat orangeball = new Mat();

        // Get the orange ball from the frame
        Imgproc.HoughCircles(frameBlur, orangeball, Imgproc.HOUGH_GRADIENT, dp, minDist, param1, param2, minRadius, maxRadius);

        // Delete the orange ball pixels from the frame, to not disturb later detection for white balls
        // TODO: Should be explored later if this is method should be used
        Core.bitwise_not(frame, frame, mask);

        // If orange ball is present, return the optional ball, else return optional empty
        if (!orangeball.empty()) {
            double[] center = orangeball.get(0, 0);
            // Create the irl coordinates and create the ball object with the Color white
            Point coordinates = new Point((center[0] - originCameraOffset.x) * conversionFactor, (center[1] - originCameraOffset.y) * conversionFactor);
            return Optional.of(new Ball(coordinates, Color.ORANGE));
        }
        return Optional.empty();
    }

    /**
     * Returns and 2 long list of coordinates. First coordinates for the biggest marker
     * the second for the next biggest one. Both on the robot.
     * The markers have to be blue.
     * @param frame that needs to be evaluated.
     * @return Returns array of points for the 2 markers.
     */
    public Point[] getRotationCoordsFromFrame(Mat frame) {
        final int areaLowerThreshold = 60;
        final int areaUpperThreshold = 350;

        // Transform frame
        Mat frameBlur = new Mat();
        Imgproc.GaussianBlur(frame, frameBlur, new Size(7,7), 7, 0);

        // Create a mask
        Mat mask = new Mat();
        Core.inRange(frameBlur, lRobot, uRobot, mask);

        HighGui.imshow("robotmask", mask);

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
        contourSets.sort(Comparator.comparingDouble(ContourSet::getArea));
        Collections.reverse(contourSets);

        for (int i = 0; i < 2; i++) { // Loop through 2 biggest contours
            MatOfPoint contour = contourSets.get(i).getContour();

            // Get bounding rectangle
            Rect rect = Imgproc.boundingRect(contour);

            // Get center of rectangles
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

    /**
     * Returns the coordinates of the border of the course.
     * @param frame to be evaluated
     * @return null if there are not found exactly 4 lines, else the 4 coordinates of the border intersections.
     */
    public BorderSet getBorderFromFrame(Mat frame) {
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

        return new BorderSet(corners, new Point(offsetX, offsetY));
    }

    private void debugGUI(Mat debugFrame) {
        Scalar ballsColor = new Scalar(255, 255, 0);
        Scalar robotColor = new Scalar(255, 0, 255);

        List<Ball> balls = course.getBalls();
        Robot robot = course.getRobot();

        // Debug Balls
        if (balls.size() > 0)
            for (Ball ball : balls) {
                // Draw Ball
                Point ballPoint = centimeterToPixel(ball.getCenter());
                Imgproc.circle(debugFrame, ballPoint, 4, ballsColor, 1);

                // Draw line from robot to balls
                if (robot != null) {
                    Point robotCenter = centimeterToPixel(robot.getCenter());
                    Imgproc.line(debugFrame, robotCenter, ballPoint, ballsColor, 1);
                }

            }

        // Debug Robot
        if (robot != null) {
            Point robotCenter = centimeterToPixel(robot.getCenter());
            Point robotRotate = centimeterToPixel(robot.getRotationMarker());

            Imgproc.circle(debugFrame, robotCenter, 5, robotColor, 2);
            Imgproc.circle(debugFrame, robotRotate, 4, robotColor, 2);
            Imgproc.line(debugFrame, robotCenter, robotRotate, robotColor, 2);
        }
    }

    private Point centimeterToPixel(Point point) {
        return new Point(point.x / conversionFactor + originCameraOffset.x, point.y / conversionFactor + originCameraOffset.y);
    }

    private Point pixelToCentimeter(Point point) {
        return new Point((point.x - originCameraOffset.x) * conversionFactor, (point.y - originCameraOffset.y) * conversionFactor);
    }
    public synchronized Course getCourse() {
        return course;
    }
}
