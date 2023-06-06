package vision.detection;

import courseObjects.Ball;
import courseObjects.Course;
import courseObjects.Robot;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import vision.helperClasses.BorderSet;
import vision.helperClasses.MaskSet;

import static vision.math.Geometry.angleBetweenTwoPoints;
import static vision.math.Geometry.distanceBetweenTwoPoints;

import java.util.ArrayList;
import java.util.List;

public class CourseDetector {
    private final int frameDelay = 1;
    protected Mat frame, overlayFrame;
    private BallDetector ballDetector;
    private BorderDetector borderDetector;
    private RobotDetector robotDetector;
    private List<SubDetector> subDetectors;


    private final boolean showMasks;
    private final Course course;

    public CourseDetector(Course course, int cameraIndex, boolean showMasks){
        this.showMasks = showMasks;
        this.course = course;

        // Initialize OpenCV
        OpenCV.loadLocally();

        // Start capture
        VideoCapture capture = new VideoCapture();
        capture.open(cameraIndex); // Open camera at index

        // Set capture resolution
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1024);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 768);

        if (!capture.isOpened()) throw new RuntimeException("Camera Capture was not opened");

        // Create Detectors and add to SubDetector List
        ballDetector = new BallDetector();
        robotDetector = new RobotDetector();
        borderDetector = new BorderDetector();

        subDetectors = new ArrayList<>();
        subDetectors.add(ballDetector);
        subDetectors.add(robotDetector);
        subDetectors.add(borderDetector);

        // Run setup to get initial objects
        // TODO runDetectionSetup(capture);

        // Run background detection
        Thread backgroundThread = new Thread(() -> detectCourse(capture));
        backgroundThread.start();
    }

    private void runDetectionSetup(VideoCapture capture) {
        while (true) {
            // Read frame
            frame = new Mat();
            capture.read(frame);

            // Throw Exception if frames are empty. Should only happen if something is truly wrong
            if (frame.empty()) throw new RuntimeException("Empty frame");

            // 1. Find corners of course to setup relative coordinates
            borderDetector.detectBorder(frame);

            // 2. Find robot before we drive
            robotDetector.detectRobot(frame);

            // 3. Find balls before we drive
            ballDetector.detectBalls(frame);

            // Display Frame to verify camera is positioned correctly and objects are seen.

        }

    }

    private void detectCourse(VideoCapture capture) {
        while (true) {
            frame = new Mat();
            capture.read(frame);

            // Get Objects (in pixel values)
            borderDetector.detectBorder(frame);
            robotDetector.detectRobot(frame);
            ballDetector.detectBalls(frame);

            // Correct Objects
            correctObjects(); // TODO

            // Push to course
            updateCourse(); // TODO

            // Debug Overlay
            showOverlay();

            // Show Masks
            if (showMasks)
                showMasks();

            // Set frame rate
            HighGui.waitKey(frameDelay);
        }
    }

    private void correctObjects() {
        // TODO nothing yet to do.
    }


    private Point[] corners;
    private double conversionFactor = 0; // Dummy value
    private Point pixelOffset;
    private void updateCourse() {
        if (borderDetector.getBorderSet() != null) {
            // Find conversion factor to translate units from pixel to CM
            corners = borderDetector.getBorderSet().getCoords();
            Point topLeft = corners[0];
            Point topRight = corners[1];

            conversionFactor = course.length / distanceBetweenTwoPoints(topLeft.x, topLeft.y, topRight.x, topRight.y);
            pixelOffset = borderDetector.getBorderSet().getOrigin();
        }

        if (corners == null) return; // Cant calculate if these are null

        updateCourseCorners();
        updateCourseRobot();
        updateCourseBalls();
    }

    private void updateCourseCorners() {
        // Set Course corners in CM
        Point[] convertedCorners = corners;
        for (int i = 0; i < corners.length; i++) {
            Point corner = corners[i];
            convertedCorners[i] = convertPixelPointToCmPoint(corner, pixelOffset);
        }

        course.setTopLeft(convertedCorners[0]);
        course.setTopRight(convertedCorners[1]);
        course.setBottomLeft(convertedCorners[2]);
        course.setBottomRight(convertedCorners[3]);
    }

    private void updateCourseRobot() {
        Robot robot = robotDetector.getRobot();

        Point correctedCenter = convertPixelPointToCmPoint(robot.getCenter(), pixelOffset);
        Point correctedFront = convertPixelPointToCmPoint(robot.getFront(), pixelOffset);
        double correctedAngle = angleBetweenTwoPoints(correctedCenter.x, correctedCenter.y, correctedFront.x, correctedFront.y);

        Robot correctedRobot = new Robot(correctedCenter, correctedFront, correctedAngle);
        course.setRobot(correctedRobot);
    }

    private void updateCourseBalls() {
        List<Ball> balls = ballDetector.getBalls();

        List<Ball> correctedBalls = new ArrayList<>();
        for (Ball ball : balls) {
            Point correctedCenter = convertPixelPointToCmPoint(ball.getCenter(), pixelOffset);

            Ball correctedBall = new Ball(correctedCenter, ball.getColor());
            correctedBalls.add(correctedBall);
        }
    }

    private Point convertPixelPointToCmPoint(Point point, Point pixelOffset) {
        return new Point((point.x - pixelOffset.x) * conversionFactor, (point.y - pixelOffset.y) * conversionFactor);
    }


    private void showOverlay() {
        // Mark objects on Overlay
        createOverlay();

        // Display overlay
        HighGui.imshow("overlay", frame);
    }

    private void createOverlay() {
        Scalar cornerColor = new Scalar(0, 255, 0);
        Scalar robotMarkerColor = new Scalar(255, 0, 255);
        Scalar ballColor = new Scalar(255, 255, 0);

        Point[] corners = null;
        BorderSet borderSet = borderDetector.getBorderSet();
        if (borderSet != null)
            corners = borderSet.getCoords();

        Robot robot = robotDetector.getRobot();
        List<Ball> balls = ballDetector.getBalls();

        overlayFrame = frame; // Create overlay frame

        // Draw Corners
        if (corners != null)
            for (Point corner : corners)
                Imgproc.circle(overlayFrame, corner, 2, cornerColor, 3);

        // Draw Robot Markers
        if (robot != null) {
            Imgproc.circle(overlayFrame, robot.getCenter(), 5, robotMarkerColor, 2);
            Imgproc.circle(overlayFrame, robot.getFront(), 4, robotMarkerColor, 2);
            Imgproc.line(overlayFrame, robot.getCenter(), robot.getFront(), robotMarkerColor, 2);
        }

        // Draw Balls
        for (Ball ball : balls) {
            Imgproc.circle(overlayFrame, ball.getCenter(), 4, ballColor, 1);
        }

        // Draw Lines between robot and balls
        if (robot != null && balls.size() > 0) {
            for (Ball ball : balls) {
                Imgproc.line(overlayFrame, robot.getCenter(), ball.getCenter(), ballColor, 1);
            }
        }
    }

    private void showMasks() {
        for (SubDetector subDetector : subDetectors) {
            for (MaskSet maskSet : subDetector.getMaskSets()) {
                HighGui.imshow(maskSet.getMaskName(), maskSet.getMask());
            }
        }
    }
}
