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
    private final int minDist = 7; // Minimum distance between balls
    private final int param1 = 30;  // gradient value used in the edge detection
    private final int param2 = 10;  // lower values allow more circles to be detected (false positives)

    private List<Ball> balls = new ArrayList<>();
    List<MaskSet> maskSets = new ArrayList<>();

    // Initialize all OpenCV objects once to not have memory leaks
    private Mat frameBlur, maskWhite1, maskOrange, frameBallsW, frameBallsO;
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
            frameBlur = new Mat();
            maskWhite1 = new Mat();
            maskOrange = new Mat();
            frameBallsW = new Mat();
            frameBallsO = new Mat();
            initial = false;
        }

        balls = new ArrayList<>();

        // Apply blur for better noise reduction
        Imgproc.GaussianBlur(frame, frameBlur, new Size(7, 7), 0);

        findWhiteBalls(balls);
        findOrangeBalls(balls);

        return !balls.isEmpty();
    }

    /**
     * Updates the balls argument with the white balls found on the frame.
     *
     * @param balls list that gets updated with newly added balls
     */
    private void findWhiteBalls(List<Ball> balls) {
        // Create mask
        Core.inRange(frameBlur, config.getLowerWhiteBallThreshold(), config.getUpperWhiteBallThreshold(), maskWhite1);

        maskSets.add(new MaskSet("White Ball Mask", maskWhite1));

        //Get white balls from frame
        Imgproc.HoughCircles(maskWhite1, frameBallsW, Imgproc.HOUGH_GRADIENT, dp, minDist, param1, param2, config.getLowerBallSize(),
            config.getUpperBallSize());

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
     * @param balls list that gets updated with newly added balls
     */
    private void findOrangeBalls(List<Ball> balls) {
        // Create mask
        Core.inRange(frameBlur, config.getLowerOrangeBallThreshold(), config.getUpperOrangeBallThreshold(), maskOrange);
        maskSets.add(new MaskSet("Orange Ball Mask", maskOrange));

        //Get white balls from frame
        Imgproc.HoughCircles(maskOrange, frameBallsO, Imgproc.HOUGH_GRADIENT, dp, minDist, param1, param2, config.getLowerBallSize(),
                config.getUpperBallSize());

        // Add orange ball to list
        if (!frameBallsO.empty()) {
            for (int i = 0; i < frameBallsO.width(); i++) {
                double[] center = frameBallsO.get(0, i);
                balls.add(new Ball(new Point(center[0], center[1]), BallColor.ORANGE, BallPickupStrategy.FREE));
            }
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
