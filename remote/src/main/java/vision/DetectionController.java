package vision;

import courseObjects.*;
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
import vision.helperClasses.MaskSet;

import static vision.math.Geometry.distanceBetweenTwoPoints;

import java.util.ArrayList;
import java.util.List;

public class DetectionController {
    private final int refreshRate = 33; // Value for best FPS (ms)
    private Mat frame, overlayFrame; // Frame to detect objects from

    // Sub Detectors
    private final List<SubDetector> subDetectors = new ArrayList<>();
    private final BallDetector ballDetector = new BallDetector();
    private final BorderDetector borderDetector = new BorderDetector();
    private final RobotDetector robotDetector = new RobotDetector();

    // For converting pixels to centimeters
    private double conversionFactorX;
    private double conversionFactorY;
    private Point pixelOffset;

    private final boolean showMasks; // Primarily for debugging
    private final Course course;

    private Border oldBorder; // Used only for creating overlay
    private final double camHeight;
    private final Point courseCenter;

    /**
     * Start a setup process that requires the different objects to be present in the camera's view.
     * When the setup is over a background thread starts doing background detection.
     * To utilize the found objects, read them from the passed Course object.
     *
     * @param course      The class that contains all the objects and information about the course during runtime.
     * @param cameraIndex The camera index of intended camera (computer specific).
     * @param showMasks   Only needed for debugging masks. If true, displays mask windows.
     */
    public DetectionController(Course course, int cameraIndex, boolean showMasks) {
        this.showMasks = showMasks;
        this.course = course;

        this.camHeight = course.getCameraHeight();
        this.courseCenter = new Point(course.getWidth() / 2, course.getHeight() / 2);

        // Initialize OpenCV
        OpenCV.loadLocally();

        // Start capture
        VideoCapture capture = new VideoCapture();
        capture.open(cameraIndex);

        setCaptureProperties(capture);

        if (!capture.isOpened()) throw new RuntimeException("Camera Capture was not opened");

        this.subDetectors.add(this.ballDetector);
        this.subDetectors.add(this.robotDetector);
        this.subDetectors.add(this.borderDetector);

        // Run setup to get initial objects
        runDetectionSetup(capture);

        startBackgroundDetection(capture);
    }

    /**
     * Sets the properties of the capture.
     * @param capture to be changed.
     */
    private void setCaptureProperties(VideoCapture capture) {
        // Resolution
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, course.getResolutionWidth());
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, course.getResolutionHeight());

        // Focus
        capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);
        capture.set(Videoio.CAP_PROP_FOCUS, 0);

        // Brightness, Contrast and Saturation
        capture.set(Videoio.CAP_PROP_BRIGHTNESS, 90);
        capture.set(Videoio.CAP_PROP_SATURATION, 180);
        capture.set(Videoio.CAP_PROP_CONTRAST, 80);
    }

    /**
     * Blocks the thread until all objects are found in the camera's view.
     *
     * @param capture the video capture from which the frame should be read.
     */
    private void runDetectionSetup(VideoCapture capture) {
        boolean borderFound = false, robotFound = false, ballFound = false;

        System.out.println("Starting Setup");

        this.frame = new Mat();
        this.overlayFrame = new Mat();

        while (true) {
            capture.read(this.frame);

            // Display frame in popup window
            showOverlay();
            if (this.showMasks)
                showMasks();
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

            updateCourse();

            // Exit when all objects are found
            System.out.println("Exiting Setup");
            break;
        }
    }

    /**
     * Spawns a thread that will run in the background. This thread runs detections and updates the course when objects
     * are found. (E.g. when the robot moves)
     *
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
     *
     * @param capture the video capture from which the frame should be read.
     */
    private void detectCourse(VideoCapture capture) {
        // Grab frame
        capture.read(this.frame);

        // Run sub detectors. They store the objects
        this.borderDetector.detectBorder(this.frame);
        this.robotDetector.detectRobot(this.frame);
        this.ballDetector.detectBalls(this.frame);

        categorizeBallsPickupStrategy(
                this.ballDetector.getBalls(),
                this.borderDetector.getCross()
        );

        updateCourse();
        showOverlay();

        // Display masks for debugging
        if (this.showMasks)
            showMasks();

        // Open all window pop-ups
        HighGui.waitKey(this.refreshRate);
    }

    /**
     * Categorized the picku strategy for each ball, depending on the closeness to the course corners and object
     * provided in the arguments.
     * @param balls The balls to be categorized
     * @param cross The cross within the border.
     */
    private void categorizeBallsPickupStrategy(List<Ball> balls, Cross cross) {
        Border border = this.borderDetector.getBorder();

        if (border == null) return;

        final double centimeterMargin = 5;

        // Convert margin to pixels
        final double pixelMarginX = centimeterMargin / conversionFactorX;
        final double pixelMarginY = centimeterMargin / conversionFactorY;

        // Get corners, TopLeft, TopRight, BottomLeft
        Point[] corners = border.getCornersAsArray();
        Point TL = corners[0], TR = corners[1], BL = corners[2];

        for (Ball ball : balls) {
            Point position = ball.getCenter();

            // Close to Top Border
            if (position.y <= TL.y + pixelMarginY) {
                ball.setStrategy(BallPickupStrategy.BORDER);
            }

            // Close to Bottom Border
            if (position.y >= BL.y - pixelMarginY) {
                // If already in another border, up the strategy to corner.
                if (ball.getStrategy() == BallPickupStrategy.BORDER)
                    ball.setStrategy(BallPickupStrategy.CORNER);
                else
                    ball.setStrategy(BallPickupStrategy.BORDER);
            }

            // Close to Right Border
            if (position.x >= TR.x - pixelMarginX) {
                // If already in another border, up the strategy to corner.
                if (ball.getStrategy() == BallPickupStrategy.BORDER)
                    ball.setStrategy(BallPickupStrategy.CORNER);
                else
                    ball.setStrategy(BallPickupStrategy.BORDER);
            }

            // Close to Left Border
            if (position.x <= TL.x + pixelMarginX) {
                // If already in another border, up the strategy to corner.
                if (ball.getStrategy() == BallPickupStrategy.BORDER)
                    ball.setStrategy(BallPickupStrategy.CORNER);
                else
                    ball.setStrategy(BallPickupStrategy.BORDER);
            }

            // For the cross (Circle hit box) | (x - center.x)^2 + (y - center.y)^2 < radius^2
            Point crossCenter = cross.getMiddle();
            if (crossCenter != null) {
                double radius = (cross.getLongestSide() / 2 + centimeterMargin) / conversionFactorX;

                if (Math.pow(position.x - crossCenter.x, 2) + Math.pow(position.y - crossCenter.y , 2) < Math.pow(radius, 2))
                    ball.setStrategy(BallPickupStrategy.CROSS);
            }
        }
    }

    /**
     * Updates the Course object with the objects detected from the sub detectors.
     * This converts the pixel values to centimetres, so that the course only has real world units.
     */
    private void updateCourse() {
        Border border = this.borderDetector.getBorder();

        // Find the corners at least once to allow updating of other course objects
        if (border != null) { // True when a border is found

            // Calculate conversion factors and get offset
            this.conversionFactorX = this.course.getWidth() / distanceBetweenTwoPoints(border.getTopLeft().x, border.getTopLeft().y,
                    border.getTopRight().x, border.getTopRight().y);

            this.conversionFactorY = this.course.getHeight() / distanceBetweenTwoPoints(border.getTopLeft().x, border.getTopLeft().y,
                    border.getBottomLeft().x, border.getBottomLeft().y);

            this.pixelOffset = this.borderDetector.getCameraOffset();

            // Update new Course's objects
            Course newCourse = new Course();
            updateNewCourseBorder(newCourse.getBorder());
            updateNewCourseRobot(newCourse.getRobot());
            updateNewCourseBalls(newCourse.getBalls());
            updateNewCourseCross(newCourse.getCross());

            // Update Real Course
            course.replaceObjects(newCourse);
        }
    }

    /**
     * Updates the Border in the Course object, by converting to centimeters and correction coordinates using height.
     */
    private void updateNewCourseBorder(Border newBorder) {
        Border pixelBorder = this.borderDetector.getBorder();
        Point[] pixelCorners = pixelBorder.getCornersAsArray(); // From pixel Border
        Point[] correctedCorners = new Point[4];

        // Save border to use for overlay
        oldBorder = pixelBorder;

        // Convert from pixel to cm.
        for (int i = 0; i < pixelCorners.length; i++) {
            correctedCorners[i] = convertPixelPointToCmPoint(pixelCorners[i], this.pixelOffset);
            correctedCorners[i] = Algorithms.correctedCoordinatesOfObject(correctedCorners[i], courseCenter, newBorder.height, camHeight);
        }

        newBorder.setTopLeft(correctedCorners[0]);
        newBorder.setTopRight(correctedCorners[1]);
        newBorder.setBottomLeft(correctedCorners[2]);
        newBorder.setBottomRight(correctedCorners[3]);
    }

    /**
     * Updates the Course robot's position, by converting to centimeters and correction coordinates using height.
     */
    private void updateNewCourseRobot(Robot newRobot) {
        // Convert from pixel to centimetres
        Robot pixelRobot = this.robotDetector.getRobot();
        Point correctedCenter = convertPixelPointToCmPoint(pixelRobot.getCenter(), this.pixelOffset);
        Point correctedFront = convertPixelPointToCmPoint(pixelRobot.getFront(), this.pixelOffset);

        correctedCenter = Algorithms.correctedCoordinatesOfObject(correctedCenter, courseCenter, newRobot.height, camHeight);
        correctedFront = Algorithms.correctedCoordinatesOfObject(correctedFront, courseCenter, newRobot.height, camHeight);

        // Update new Robot
        newRobot.setFrontAndCenter(correctedCenter, correctedFront);
    }

    /**
     * Updates the Course's balls positions, by converting to centimeters and correction coordinates using height.
     */
    private void updateNewCourseBalls(List<Ball> newBalls) {
        List<Ball> pixelBalls = this.ballDetector.getBalls();
        for (Ball ball : pixelBalls) {
            // Convert position from pixel to cm
            Point correctedCenter = convertPixelPointToCmPoint(ball.getCenter(), this.pixelOffset);

            // Correct by height
            correctedCenter = Algorithms.correctedCoordinatesOfObject(correctedCenter, courseCenter, ball.getRadius(), camHeight);

            // Update New Balls
            Ball correctedBall = new Ball(correctedCenter, ball.getColor(), ball.getStrategy());
            newBalls.add(correctedBall);
        }
    }

    /**
     * Updates the Course's Cross object, by converting to centimeters and correction coordinates using height.
     */
    private void updateNewCourseCross(Cross newCross) {
        if (newCross.getMiddle() != null && newCross.getMeasurePoint() != null) {
            // Convert to CM
            Cross pixelCross = this.borderDetector.getCross();
            Point correctedMiddle = convertPixelPointToCmPoint(pixelCross.getMiddle(), this.pixelOffset);
            Point correctedMeasurePoint = convertPixelPointToCmPoint(pixelCross.getMeasurePoint(), this.pixelOffset);

            // Correct using height
            correctedMiddle = Algorithms.correctedCoordinatesOfObject(correctedMiddle, courseCenter, newCross.getHeight(), camHeight);
            correctedMeasurePoint = Algorithms.correctedCoordinatesOfObject(correctedMeasurePoint, courseCenter, newCross.getHeight(), camHeight);

            newCross.setMiddle(correctedMiddle);
            newCross.setMeasurePoint(correctedMeasurePoint);
        }
    }

    /**
     * Converts Point from pixel units to centimetres and subtracts a pixel offset.
     *
     * @param point       Point to be converted.
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
        // Display overlay
        createOverlay();
        HighGui.imshow("overlay", this.overlayFrame);
    }

    /**
     * Draws an overlay on the frame and puts it in the display pile.
     */
    private void createOverlay() {
        // Define colors for different objects
        Scalar cornerColor = new Scalar(0, 255, 0); // Green
        Scalar robotMarkerColor = new Scalar(255, 0, 255); // Magenta
        Scalar ballColor = new Scalar(255, 255, 0); // Cyan
        Scalar crossColor = new Scalar(0, 255, 255); // Yellow

        this.overlayFrame = this.frame;

        // Draw Corners
        Point[] corners = oldBorder != null ? oldBorder.getCornersAsArray() : null;

        if (corners != null)
            for (Point corner : corners)
                Imgproc.circle(this.overlayFrame, corner, 2, cornerColor, 3);

        // Draw the middle of the cross
        Cross cross = this.borderDetector.getCross();
        if (cross != null) {
            Point middle = cross.getMiddle();
            if (middle != null)
                Imgproc.circle(this.overlayFrame, middle, 2, crossColor, 3);
            Point measurePoint = cross.getMeasurePoint();
            if (measurePoint != null)
                Imgproc.circle(this.overlayFrame, measurePoint, 2, crossColor, 3);
        }

        // Draw Robot Markers
        Robot robot = this.robotDetector.getRobot();

        if (robot != null) {
            Imgproc.circle(this.overlayFrame, robot.getCenter(), 5, robotMarkerColor, 2);
            Imgproc.circle(this.overlayFrame, robot.getFront(), 4, robotMarkerColor, 2);
            Imgproc.line(this.overlayFrame, robot.getCenter(), robot.getFront(), robotMarkerColor, 2);
        }

        // Draw Balls
        List<Ball> balls = this.ballDetector.getBalls();

        for (Ball ball : balls) {
            Imgproc.circle(this.overlayFrame, ball.getCenter(), 4, ballColor, 1);

            // Draw Lines between robot and balls
            if (robot != null)
                Imgproc.line(this.overlayFrame, robot.getCenter(), ball.getCenter(), ballColor, 1);
        }
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
}
