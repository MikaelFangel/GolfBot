package vision.detection;

import courseObjects.Ball;
import courseObjects.BallColor;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import vision.BallPickupStrategy;
import vision.helperClasses.MaskSet;

import java.util.ArrayList;
import java.util.List;

public class BallDetector implements SubDetector {
    // HoughCircles parameters. These configurations works okay with the current course setup (Most likely pixel values)
    private final int dp = 1; // Don't question or change
    private final int minDist = 5; // Minimum distance between balls
    private final int param1 = 20;  // gradient value used in the edge detection
    private final int param2 = 12;  // lower values allow more circles to be detected (false positives)

    private List<Ball> balls = new ArrayList<>();
    List<MaskSet> maskSets = new ArrayList<>();

    // Initialize all OpenCV objects once to not have memory leaks
    private Mat frameGray, frameBlur, mask, frameBallsW, frameBallsO, frameHSV;
    private boolean initial = true;

    private final DetectionConfiguration config = DetectionConfiguration.DetectionConfiguration();

    /**
     * Detects the balls on the frame
     *
     * @param frame the frame to evaluated
     * @return A boolean symbolizing if balls were found or not
     */
    public boolean detectBalls(Mat frame) {
        // Initialize all OpenCV objects once to not have memory leaks
        if (initial) {
            frameGray = new Mat();
            frameBlur = new Mat();
            mask = new Mat();
            frameBallsW = new Mat();
            frameBallsO = new Mat();
            frameHSV = new Mat();

            initial = false;
        }

        balls = new ArrayList<>();

        findWhiteBalls(frame, balls);
        //findOrangeBalls(frame, balls); TODO! Fix me

        return !balls.isEmpty();
    }

    /**
     * Updates the balls argument with the white balls found on the frame.
     *
     * @param frame to be used for detection
     * @param balls list that gets updated with newly added balls
     */
    private void findWhiteBalls(Mat frame, List<Ball> balls) {
        // Apply gray frame for detecting white balls
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        // Apply a binary threshold mask to separate out all colors than white.
        Imgproc.threshold(frameGray, mask, config.getLowerBallThreshold(), config.getUpperBallThreshold(), Imgproc.THRESH_BINARY);

        // Create mask set for debugging
        maskSets.add(new MaskSet("whiteBalls Mask", mask));

        // Apply blur for better noise reduction
        Imgproc.GaussianBlur(mask, frameBlur, new Size(7, 7), 0);

        // Get white balls from frame
        Imgproc.HoughCircles(frameBlur, frameBallsW, Imgproc.HOUGH_GRADIENT, dp, minDist, param1, param2, config.getLowerBallSize(),
                config.getUpperBallSize()); // Approximate circles on the frame. Middle coordinates is stored in first row

        // Add balls to array
        if (!frameBallsW.empty()) {
            for (int i = 0; i < frameBallsW.width(); i++) {
                double[] center = frameBallsW.get(0, i);
                balls.add(new Ball(new Point(center[0], center[1]), BallColor.WHITE, BallPickupStrategy.FREE));
            }
        }
    }

    /**
     * Updates the balls argument with the white balls found on the frame.
     *
     * @param frame to be used for detection
     * @param balls list that gets updated with newly added balls
     */
    private void findOrangeBalls(Mat frame, List<Ball> balls) {
        // Apply hsv filter to distinguish orange ball
        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);

        // Orange balls threshold (BGR)
        final Scalar lOrangeBall = new Scalar(0, 100, 220);
        final Scalar uOrangeBall = new Scalar(170, 255, 255);

        // Create a mask to separate the orange ball
        Core.inRange(frameHSV, lOrangeBall, uOrangeBall, mask);

        // Create MaskSet for debugging
        maskSets.add(new MaskSet("orangeBallsMask", mask));

        // Apply blur for noise reduction
        Imgproc.GaussianBlur(mask, frameBlur, new Size(7, 7), 0);

        // Stores orange circles from the HoughCircles algorithm
        // Get the orange ball from the frame
        Imgproc.HoughCircles(frameBlur, frameBallsO, Imgproc.HOUGH_GRADIENT, dp, minDist, param1, param2, config.getLowerBallSize(), config.getUpperBallSize());

        // Delete the orange ball pixels from the frame, to not disturb later detection for white balls
        // TODO: Should be explored later if this is method should be used
        Core.bitwise_not(frame, frame, mask);

        // Add orange ball to list
        if (!frameBallsO.empty()) {
            double[] center = frameBallsO.get(0, 0);
            Point coords = new Point(center[0], center[1]);

            balls.add(new Ball(coords, BallColor.ORANGE, BallPickupStrategy.FREE));
        }
    }

    public List<Ball> getBalls() {
        return balls;
    }

    @Override
    public List<MaskSet> getMaskSets() {
        return maskSets;
    }
}
