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
import vision.helperClasses.MaskSet;

import java.util.List;

public class CourseDetector {
    private final int frameDelay = 1;
    protected Mat frame, overlayFrame;
    private BallDetector ballDetector;
    private BorderDetector borderDetector;
    private RobotDetector robotDetector;
    private List<SubDetector> subDetectors;


    public CourseDetector(Course course, int cameraIndex){
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
        subDetectors.add(ballDetector);
        subDetectors.add(robotDetector);
        subDetectors.add(borderDetector);

        // Run setup to get initial objects
        runDetectionSetup(capture);

        // Run background detection
        Thread backgroundThread = new Thread(() -> detectCourse(capture));
        backgroundThread.start();
    }

    public void runDetectionSetup(VideoCapture capture) {
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

    public void detectCourse(VideoCapture capture) {
        while (true) {
            frame = new Mat();
            capture.read(frame);

            // Get Objects (in pixel values)
            borderDetector.detectBorder(frame);
            robotDetector.detectRobot(frame);
            ballDetector.detectBalls(frame);

            // Correct Objects
            correctObjects();

            // Push to course
            updateCourse();

            // Debug Overlay
            showOverlay();

            // Show Masks
            showMasks();

            // Set frame rate
            HighGui.waitKey(frameDelay);
        }
    }

    private void correctObjects() {

    }

    private void updateCourse() {
        // Make coordinates to CM
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

        Point[] corners = borderDetector.getBorderSet().getCorrectCoords();
        Robot robot = robotDetector.getRobot();
        List<Ball> balls = ballDetector.getBalls();

        overlayFrame = frame; // Create overlay frame

        // Draw Corners
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
