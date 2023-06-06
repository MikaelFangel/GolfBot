package vision;

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
import vision.detection.BallDetector;
import vision.detection.BorderDetector;
import vision.detection.RobotDetector;
import vision.detection.SubDetector;
import vision.helperClasses.BorderSet;
import vision.helperClasses.MaskSet;

import static vision.math.Geometry.angleBetweenTwoPoints;
import static vision.math.Geometry.distanceBetweenTwoPoints;

import java.util.ArrayList;
import java.util.List;

public class DetectionController {
    private final int refreshRate = 1; // Value for best FPS (ms)
    private Mat frame; // Frame to detect objects from

    // Sub Detectors
    private final List<SubDetector> subDetectors = new ArrayList<>();
    private final BallDetector ballDetector = new BallDetector();
    private final BorderDetector borderDetector = new BorderDetector();
    private final RobotDetector robotDetector = new RobotDetector();

    // For converting pixels to centimeters
    private Point[] corners;
    private double conversionFactorX;
    private double conversionFactorY;
    private Point pixelOffset;

    private final boolean showMasks; // Primarily for debugging
    private final Course course;

    /**
     * Start a setup process that requires the different objects to be present in the camera's view.
     * When the setup is over a background thread starts doing background detection.
     * To utilize the found objects, read them from the passed Course object.
     * @param course The class that contains all the objects and information about the course during runtime.
     * @param cameraIndex The camera index of intended camera (computer specific).
     * @param showMasks Only needed for debugging masks. If true, displays mask windows.
     */
    public DetectionController(Course course, int cameraIndex, boolean showMasks) {
        this.showMasks = showMasks;
        this.course = course;

        // Initialize OpenCV
        OpenCV.loadLocally();

        // Start capture
        VideoCapture capture = new VideoCapture();
        capture.open(cameraIndex);

        // Set capture resolution
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1024);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 768);

        if (!capture.isOpened()) throw new RuntimeException("Camera Capture was not opened");

        // Add detectors to list
        this.subDetectors.add(ballDetector);
        this.subDetectors.add(robotDetector);
        this.subDetectors.add(borderDetector);

        // Run setup to get initial objects
        runDetectionSetup(capture);

        startBackgroundDetection(capture);
    }

    /**
     * Blocks the thread until all objects are found in the camera's view.
     * @param capture the video capture from which the frame should be read.
     */
    private void runDetectionSetup(VideoCapture capture) {
        boolean borderFound = false, robotFound = false, ballFound = false;

        System.out.println("Starting Setup");

        while (true) {
            frame = new Mat();
            capture.read(this.frame);

            // Display frame in popup window
            showOverlay();
            HighGui.waitKey(this.refreshRate);

            // Run sub detectors. To get objects in necessary order
            if (!borderFound) {
                borderFound = this.borderDetector.detectBorder(this.frame);
                if (!borderFound) continue;

                System.out.println("Found Corners");
            }

            if (!robotFound) {
                robotFound = this.robotDetector.detectRobot(this.frame);
                if (!robotFound) continue;

                System.out.println("Found Robot");
            }

            if (!ballFound) {
                ballFound = this.ballDetector.detectBalls(this.frame);
                if (!ballFound) continue;

                System.out.println("Found least a ball");
            }

            // Exit when all objects are found
            System.out.println("Exiting Setup");
            break;
        }
    }

    /**
     * Spawns a thread that will run in the background. This thread runs detections and updates the course when objects
     * are found. (E.g. when the robot moves)
     * @param capture the video capture from which the frame should be read.
     */
    private void startBackgroundDetection(VideoCapture capture) {
        System.out.println("Start Background Detection");

        new Thread(() -> {
            while (true)
                detectCourse(capture);
        }).start();
    }

    /**
     * Runs all the sub detectors to detect objects on the course.
     * The objects gets corrected using different algorithms (E.g. height correction).
     * Then the objects gets converted to real world units (cm) and updates the Course object.
     * The frames will get displayed.
     * @param capture the video capture from which the frame should be read.
     */
    private void detectCourse(VideoCapture capture) {
        // Grab frame
        frame = new Mat();
        capture.read(this.frame);

        // Run sub detectors. They store the objects
        this.borderDetector.detectBorder(this.frame);
        this.robotDetector.detectRobot(this.frame);
        this.ballDetector.detectBalls(this.frame);

        correctObjects();
        updateCourse();
        showOverlay();

        // Display masks for debugging
        if (this.showMasks)
            showMasks();

        // Open all window pop-ups
        HighGui.waitKey(this.refreshRate);
    }

    private void correctObjects() {
        // TODO nothing yet to do.
    }

    /**
     * Updates the Course object with the objects detected from the sub detectors.
     * This converts the pixel values to centimetres, so that the course only has real world units.
     */
    private void updateCourse() {
        BorderSet borderSet = this.borderDetector.getBorderSet();

        // Find the corners at least once to allow updating of other course objects
        if (borderSet != null) { // True when a border is found

            // Find conversion factor to translate units from pixel to CM
            this.corners = borderSet.getCoords().clone();
            Point topLeft = corners[0];
            Point topRight = corners[1];
            Point bottomLeft = corners[2];

            // Calculate conversion factors and get offset
            this.conversionFactorX = this.course.length / distanceBetweenTwoPoints(topLeft.x, topLeft.y, topRight.x, topRight.y);
            this.conversionFactorY = this.course.width / distanceBetweenTwoPoints(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y);
            this.pixelOffset = borderSet.getOrigin();
        }

        if (this.corners == null) return; // Cant calculate if these are null

        updateCourseCorners();
        updateCourseRobot();
        updateCourseBalls();
    }

    /**
     * Updates the corner positions of Course object, in centimetres.
     */
    private void updateCourseCorners() {
        Point[] convertedCorners = this.corners.clone();

        // Convert from pixel to cm.
        for (int i = 0; i < convertedCorners.length; i++)
            convertedCorners[i] = convertPixelPointToCmPoint(convertedCorners[i], this.pixelOffset);

        // Update Course
        this.course.setTopLeft(convertedCorners[0]);
        this.course.setTopRight(convertedCorners[1]);
        this.course.setBottomLeft(convertedCorners[2]);
        this.course.setBottomRight(convertedCorners[3]);
    }

    /**
     * Updates the Course robot's position, in centimetres.
     */
    private void updateCourseRobot() {
        Robot robot = this.robotDetector.getRobot();

        // Convert from pixel to centimetres
        Point correctedCenter = convertPixelPointToCmPoint(robot.getCenter(), this.pixelOffset);
        Point correctedFront = convertPixelPointToCmPoint(robot.getFront(), this.pixelOffset);
        double correctedAngle = angleBetweenTwoPoints(correctedCenter.x, correctedCenter.y, correctedFront.x, correctedFront.y);

        Robot correctedRobot = new Robot(correctedCenter, correctedFront, correctedAngle);
        this.course.setRobot(correctedRobot);
    }

    /**
     * Updates the Course's balls positions
     */
    private void updateCourseBalls() {
        List<Ball> balls = this.ballDetector.getBalls();
        List<Ball> correctedBalls = new ArrayList<>();

        // Convert position from pixel to cm
        for (Ball ball : balls) {
            Point correctedCenter = convertPixelPointToCmPoint(ball.getCenter(), this.pixelOffset);

            Ball correctedBall = new Ball(correctedCenter, ball.getColor());
            correctedBalls.add(correctedBall);
        }

        this.course.setBalls(correctedBalls);
    }

    /**
     * Converts Point from pixel units to centimetres and subtracts a pixel offset.
     * @param point Point to be converted.
     * @param pixelOffset The offset to be subtracted before the multiplication of the factor.
     * @return The new converted point in centimetres.
     */
    private Point convertPixelPointToCmPoint(Point point, Point pixelOffset) {
        return new Point((point.x - pixelOffset.x) * this.conversionFactorX, (point.y - pixelOffset.y) * this.conversionFactorY);
    }

    /**
     * Displays the frames with an overlay
     */
    private void showOverlay() {
        Mat overlayFrame = createOverlay();

        // Display overlay
        HighGui.imshow("overlay", overlayFrame);
    }

    /**
     * Draws an overlay on the frame and puts it in the display pile.
     */
    private Mat createOverlay() {
        // Define colors for different objects
        Scalar cornerColor = new Scalar(0, 255, 0); // Green
        Scalar robotMarkerColor = new Scalar(255, 0, 255); // Magenta
        Scalar ballColor = new Scalar(255, 255, 0); // Cyan

        Mat overlayFrame = this.frame.clone();

        // Draw Corners
        BorderSet borderSet = this.borderDetector.getBorderSet();
        Point[] corners = borderSet != null ? borderSet.getCoords() : null;

        if (corners != null)
            for (Point corner : corners)
                Imgproc.circle(overlayFrame, corner, 2, cornerColor, 3);

        // Draw Robot Markers
        Robot robot = this.robotDetector.getRobot();

        if (robot != null) {
            Imgproc.circle(overlayFrame, robot.getCenter(), 5, robotMarkerColor, 2);
            Imgproc.circle(overlayFrame, robot.getFront(), 4, robotMarkerColor, 2);
            Imgproc.line(overlayFrame, robot.getCenter(), robot.getFront(), robotMarkerColor, 2);
        }

        // Draw Balls
        List<Ball> balls = this.ballDetector.getBalls();

        for (Ball ball : balls) {
            Imgproc.circle(overlayFrame, ball.getCenter(), 4, ballColor, 1);

            // Draw Lines between robot and balls
            if (robot != null)
                Imgproc.line(overlayFrame, robot.getCenter(), ball.getCenter(), ballColor, 1);
        }

        return overlayFrame;
    }

    /**
     * Adds the different masks to the display pile.
     * Debugging Tool
     */
    private void showMasks() {
        for (SubDetector subDetector : this.subDetectors) {
            for (MaskSet maskSet : subDetector.getMaskSets()) {
                HighGui.imshow(maskSet.getMaskName(), maskSet.getMask());
            }
        }
    }

    public Course getCourse() {
        return this.course;
    }
}
