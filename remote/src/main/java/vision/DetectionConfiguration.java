package vision;

import org.opencv.core.Scalar;

public class DetectionConfiguration {
    private static DetectionConfiguration single_instance = null;

    // Balls
    double lowerBallThreshold = 205;
    double upperBallThreshold = 255;
    int lowerBallSize = 3;
    int upperBallSize = 10;

    // Robot (Blue)
    final Scalar lowerRobotThreshold = new Scalar(130, 90, 20);
    final Scalar upperRobotThreshold = new Scalar(255, 200, 110);
    int lowerRobotSize = 350;
    int upperRobotSize = 1300;

    // Obstacles (Red)
    Scalar lowerObstacleThreshold = new Scalar(0, 0, 160);
    Scalar upperObstacleThreshold = new Scalar(100, 100, 255);

    private DetectionConfiguration() {

    }

    public static DetectionConfiguration DetectionConfiguration() {
        if (single_instance == null)
            single_instance = new DetectionConfiguration();

        return single_instance;
    }

    //                  Robot                   //
    public Scalar getLowerRobotThreshold() {
        return lowerRobotThreshold;
    }

    public Scalar getUpperRobotThreshold() {
        return upperRobotThreshold;
    }

    public double getLowerRobotSize() {
        return lowerRobotSize;
    }

    public double getUpperRobotSize() {
        return upperRobotSize;
    }


    //                  Balls                   //
    public double getLowerBallThreshold() {
        return lowerBallThreshold;
    }

    public double getUpperBallThreshold() {
        return upperBallThreshold;
    }

    public int getLowerBallSize() {
        return lowerBallSize;
    }

    public int getUpperBallSize() {
        return upperBallSize;
    }

    //                  Border & Cross          //
    public Scalar getLowerObstacleThreshold() {
        return lowerObstacleThreshold;
    }

    public Scalar getUpperObstacleThreshold() {
        return upperObstacleThreshold;
    }
}