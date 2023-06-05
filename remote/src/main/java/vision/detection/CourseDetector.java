package vision.detection;

import courseObjects.Course;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CourseDetector {
    protected Mat frame, overlayFrame;

    private BallDetector ballDetector;
    private BorderDetector borderDetector;
    private RobotDetector robotDetector;

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

        // Create Detectors
        ballDetector = new BallDetector(course);
        robotDetector = new RobotDetector(course);
        borderDetector = new BorderDetector(course);

        // Run setup to get initial objects
        runDetectionSetup(capture);

        // Run background detection
        Thread backgroundThread = new Thread(() -> detectCourse(capture));
        backgroundThread.start();
    }

    public void runDetectionSetup(VideoCapture capture) {

    }

    public void detectCourse(VideoCapture capture) {

    }
}
