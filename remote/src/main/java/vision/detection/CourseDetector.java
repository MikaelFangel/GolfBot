package vision.detection;

import courseObjects.Ball;
import courseObjects.Course;
import courseObjects.Robot;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import vision.helperClasses.BorderSet;
import vision.helperClasses.MaskSet;

import java.util.List;

public class CourseDetector {
    private final int frameDelay = 1;
    protected Mat frame, overlayFrame;
    private BallDetector ballDetector;
    private BorderDetector borderDetector;
    private RobotDetector robotDetector;
    private List<SubDetector> subDetectors;

    // All values in these are stored as pixel values
    private BorderSet borderSet;
    private Robot robot;
    private List<Ball> balls;


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
            borderSet = borderDetector.detectBorder(frame);

            // 2. Find robot before we drive
            robot = robotDetector.detectRobot(frame);

            // 3. Find balls before we drive
            balls = ballDetector.detectBalls(frame);

            // Display Frame to verify camera is positioned correctly and objects are seen.

        }

    }

    public void detectCourse(VideoCapture capture) {
        while (true) {
            frame = new Mat();
            capture.read(frame);

            // Get Objects (in pixel values)
            borderSet = borderDetector.detectBorder(frame);
            robot = robotDetector.detectRobot(frame);
            balls = ballDetector.detectBalls(frame);

            // Correct Objects
            correctObjects();

            // Push to course
            updateCourse();

            // Debug Overlay
            showOverlay();
        }
    }

    private void correctObjects() {

    }

    private void updateCourse() {
        // Make coordinates to CM
    }

    private void showOverlay() {
        // Mark objects on Overlay

        // Display overlay
        HighGui.imshow("overlay", frame);


        // Display all Masks
        for (SubDetector subDetector : subDetectors) {
            for (MaskSet maskSet : subDetector.getMaskSets()) {
                HighGui.imshow(maskSet.getMaskName(), maskSet.getMask());
            }
        }

        // Set frame rate
        HighGui.waitKey(frameDelay);
    }
}
