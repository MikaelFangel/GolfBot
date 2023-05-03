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

import static vision.Calculations.angleBetweenTwoPoints;
import static vision.Calculations.distanceBetweenTwoPoints;

public class Detection {

    private final Course course;
    private double conversionFactor;
    private Point originCameraOffset;

    private final Thread backgroundThread;

    // HoughCircles parameters. These configurations works okay with the current course setup
    private final int minDist = 5;
    private final int param1 = 20;  // gradient value used in the edge detection
    private final int param2 = 12;  // lower values allow more circles to be detected (false positives)
    private final int minRadius = 1;  // limits the smallest circle to this size (via radius)
    private final int maxRadius = 6;  // similarly sets the limit for the largest circles

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
        Point[] cornerCoords = borderSet.correctCoords;


        topLeft = new Point(cornerCoords[0].x, cornerCoords[0].y);
        topRight = new Point(cornerCoords[1].x, cornerCoords[1].y);
        bottomRight = new Point(cornerCoords[2].x, cornerCoords[2].y);
        bottomLeft = new Point(cornerCoords[3].x, cornerCoords[3].y);

        // Calculate conversion factor and save origin offset for
        conversionFactor = course.length / distanceBetweenTwoPoints(topLeft.x, topLeft.y, topRight.x, topRight.y);
        originCameraOffset = borderSet.origin;

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

    private Optional<Ball> findOrangeBall(Mat frame) {
        // Start finding the orange ball
        Mat frameHsv = new Mat();
        Imgproc.cvtColor(frame, frameHsv, Imgproc.COLOR_BGR2HSV);

        // Create a mask to seperate the orange ball
        Mat mask = new Mat();
        Scalar lower = new Scalar(10, 130, 220);
        Scalar upper = new Scalar(255, 255, 255);
        Core.inRange(frameHsv, lower, upper, mask);

        // Stores orange circles from the HoughCircles algorithm
        Mat orangeball = new Mat();

        // Get the orange ball from the frame
        Imgproc.HoughCircles(mask, orangeball, Imgproc.HOUGH_GRADIENT, minDist, param1, 20, 15, 2, 7);

        // Add orangeball to balls arraylist
        if (!orangeball.empty()) {
            double[] center = orangeball.get(0, 0);
            // Create the irl coordinates and create the ball object with the Color white
            Point coordinates = new Point((center[0] - originCameraOffset.x) * conversionFactor, (center[1] - originCameraOffset.y) * conversionFactor);
            Imgproc.circle(frame, coordinates, 5, new Scalar(0, 0, 255), 1);
            return Optional.of(new Ball(coordinates, Color.ORANGE));
        }
        return Optional.empty();
    }

    private boolean findBalls(Mat frame, Mat debugFrame) {
        ArrayList<Ball> balls = new ArrayList<>();

        Optional<Ball> orangeBall = findOrangeBall(frame);
        orangeBall.ifPresent(balls::add);

        // Gray
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        // Now search for white balls. Apply a binary threshold mask to seperate out all colors than white.
        Mat binaryFrame = new Mat();
        Imgproc.threshold(frameGray, binaryFrame, 200, 255, Imgproc.THRESH_BINARY);

        // Apply blur for better noise reduction
        Mat frameBlur = new Mat();
        // Apply blur for better noise detection
        Imgproc.GaussianBlur(binaryFrame, frameBlur, new Size(7,7), 0);

        HighGui.imshow("frame", binaryFrame);
        //HighGui.waitKey(1);

        // Get white balls from frame
        Mat whiteballs = new Mat();
        Imgproc.HoughCircles(frameBlur, whiteballs, Imgproc.HOUGH_GRADIENT, 1, 30, 20, 13, 1, 7);

        // Add whiteballs to balls arraylist
        if (!whiteballs.empty()) {
            for (int i = 0; i < whiteballs.width(); i++) {
                double[] center = whiteballs.get(0, i);
                // Create the irl coordinates and create the ball object with the Color white
                Point coordinates = new Point((center[0] - originCameraOffset.x) * conversionFactor, (center[1] - originCameraOffset.y) * conversionFactor);
                balls.add(new Ball(coordinates, Color.WHITE));
            }
        }

        // Update ball positions
        if (balls.size() == 0) return false;

        course.setBalls(balls);
        return true;
    }

    /**
     * Returns a Point array of center coordinates for each circle found on the board.
     * @param frame to be evaluated.
     * @return Point array, with coordinates of the center for each circle.
     */
    public Point[] getWhiteBallCoordsFromFrame(Mat frame) {
        //Converting the image to Gray and blur it
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        Mat binaryFrame = new Mat();
        Imgproc.threshold(frameGray, binaryFrame, 185, 255, Imgproc.THRESH_BINARY);

        Mat frameBlur = new Mat();
        Imgproc.GaussianBlur(binaryFrame, frameBlur, new Size(7,7), 0);

        ArrayList<double[]> circleCoords = new ArrayList<>();

        // Get circles from frame
        Mat circles = new Mat();
        Imgproc.HoughCircles(frameGray, circles, Imgproc.HOUGH_GRADIENT, 1, 50, 20, 10, 1, 6);

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
    public Point[] getRotationCoordsFromFrame(Mat frame) {
        final int areaLowerThreshold = 60;
        final int areaUpperThreshold = 350;

        // Transform frame
        Mat frameHSV = new Mat();
        Mat frameBlur = new Mat();

        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);
        Imgproc.GaussianBlur(frameHSV, frameBlur, new Size(7,7), 7, 0);

        // Create a mask
        Mat mask = new Mat();
        Scalar lower = new Scalar(100, 100, 120);
        Scalar upper = new Scalar(255, 255, 255);
        Core.inRange(frameBlur, lower, upper, mask);

        //HighGui.imshow("robotmask", mask);

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
                    Point robotCenter = centimeterToPixel(robot.center);
                    Imgproc.line(debugFrame, robotCenter, ballPoint, ballsColor, 1);
                }

            }

        // Debug Robot
        if (course.getRobot() != null) {
            Point robotCenter = centimeterToPixel(robot.center);
            Point robotRotate = centimeterToPixel(robot.rotationMarker);

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
