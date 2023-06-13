package vision.detection;

import org.opencv.core.Scalar;

public class DetectionConfiguration {
    private static DetectionConfiguration single_instance = null;

    // Balls
    private Scalar lowerWhiteBallThreshold = new Scalar(105, 105, 105);
    private Scalar upperWhiteBallThreshold = new Scalar(230, 230, 230);
    private Scalar lowerOrangeBallThreshold = new Scalar(0, 40, 130);
    private Scalar upperOrangeBallThreshold = new Scalar(20, 170, 255);
    private int lowerBallSize = 6;
    private int upperBallSize = 15;

    // Robot (Blue)
    private Scalar lowerRobotThreshold = new Scalar(130, 80, 0);
    private Scalar upperRobotThreshold = new Scalar(250, 120, 60);
    private int lowerRobotSize = 300;
    private int upperRobotSize = 5000;

    // Obstacles (Red)
    private Scalar lowerObstacleThreshold = new Scalar(0, 0, 100);
    private Scalar upperObstacleThreshold = new Scalar(5, 5, 255);
    private double lowerBorderSize = 100000;
    private double lowerCrossSize = 800;
    private double upperCrossSize = 4000;

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
    public Scalar getLowerWhiteBallThreshold() {
        return lowerWhiteBallThreshold;
    }

    public Scalar getUpperWhiteBallThreshold() {
        return upperWhiteBallThreshold;
    }

    public Scalar getLowerOrangeBallThreshold() {
        return lowerOrangeBallThreshold;
    }

    public Scalar getUpperOrangeBallThreshold() {
        return upperOrangeBallThreshold;
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

    public double getLowerBorderSize() {
        return lowerBorderSize;
    }

    public double getLowerCrossSize() {
        return lowerCrossSize;
    }

    public double getUpperCrossSize() {
        return upperCrossSize;
    }
}