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
    private List<Ball> balls = new ArrayList<>();
    List<MaskSet> maskSets = new ArrayList<>();

    private final DetectionConfiguration config = DetectionConfiguration.DetectionConfiguration();

    /**
     * Detects the balls on the frame
     *
     * @param frame The frame to evaluated
     */
    public void detectBalls(Mat frame) {
        // Initialize all OpenCV objects once to not have memory leaks
        Mat frameBlur = new Mat();

        balls = new ArrayList<>();

        // Apply blur for better noise reduction
        Imgproc.GaussianBlur(frame, frameBlur, new Size(11, 11), 0);

        findWhiteBalls(frameBlur, balls);
        findOrangeBalls(frameBlur, balls);

        frameBlur.release();
    }

    /**
     * Updates the balls argument with the white balls found on the frame.
     *
     * @param balls List that gets updated with newly added balls
     */
    private void findWhiteBalls(Mat frameBlur, List<Ball> balls) {
        Mat maskWhite = new Mat();
        Mat ballsMatW = new Mat();

        // Create mask
        Core.inRange(frameBlur, config.getLowerWhiteBallThreshold(), config.getUpperWhiteBallThreshold(), maskWhite);

        maskSets.add(new MaskSet("White Ball Mask", maskWhite));

        //Get white balls from frame
        Imgproc.HoughCircles(maskWhite, ballsMatW, Imgproc.HOUGH_GRADIENT,
                config.getBallDp(), config.getBallMinDist(), config.getBallParam1(), config.getBallParam2(),
                config.getLowerBallSize(), config.getUpperBallSize());

        // Add balls to array
        if (!ballsMatW.empty()) {
            for (int i = 0; i < ballsMatW.width(); i++) {
                double[] center = ballsMatW.get(0, i);

                // Make balls FREE by default. Will be changed later
                balls.add(new Ball(new Point(center[0], center[1]), BallColor.WHITE, BallPickupStrategy.FREE));
            }
        }

        maskWhite.release();
        ballsMatW.release();
    }

    /**
     * Updates the balls argument with the white balls found on the frame.
     *
     * @param balls list that gets updated with newly added balls
     */
    private void findOrangeBalls(Mat frameBlur, List<Ball> balls) {
        Mat ballsMatO = new Mat();
        Mat maskOrange = new Mat();

        // Create mask
        Core.inRange(frameBlur, config.getLowerOrangeBallThreshold(), config.getUpperOrangeBallThreshold(), maskOrange);
        maskSets.add(new MaskSet("Orange Ball Mask", maskOrange));

        //Get white balls from frame
        Imgproc.HoughCircles(maskOrange, ballsMatO, Imgproc.HOUGH_GRADIENT,
                config.getBallDp(), config.getBallMinDist(), config.getBallParam1(), config.getBallParam2(),
                config.getLowerBallSize(), config.getUpperBallSize());

        // Add orange ball to list
        if (!ballsMatO.empty()) {
            for (int i = 0; i < ballsMatO.width(); i++) {
                double[] center = ballsMatO.get(0, i);

                // Make balls FREE by default. Will be changed later
                balls.add(new Ball(new Point(center[0], center[1]), BallColor.ORANGE, BallPickupStrategy.FREE));
            }
        }

        maskOrange.release();
        ballsMatO.release();
    }

    public List<Ball> getBalls() {
        return balls;
    }

    @Override
    public List<MaskSet> getMaskSets() {
        return maskSets;
    }
}
