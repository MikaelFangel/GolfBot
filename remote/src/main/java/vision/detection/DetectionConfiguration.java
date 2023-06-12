package vision.detection;

import org.opencv.core.Scalar;

public class DetectionConfiguration {
    private static DetectionConfiguration single_instance = null;

    // Balls
    private Scalar lowerWhiteBallThreshold = new Scalar(180, 180, 180);
    private Scalar upperWhiteBallThreshold = new Scalar(255, 255, 255);
    private Scalar lowerOrangeBallThreshold = new Scalar(0, 100, 220);
    private Scalar upperOrangeBallThreshold = new Scalar(170, 255, 255);
    private int lowerBallSize = 2;
    private int upperBallSize = 8;

    // Robot (Blue)
    private Scalar lowerRobotThreshold = new Scalar(150, 90, 20);
    private Scalar upperRobotThreshold = new Scalar(230, 155, 100);
    private int lowerRobotSize = 350;
    private int upperRobotSize = 1300;

    // Obstacles (Red)
    private Scalar lowerObstacleThreshold = new Scalar(0, 0, 160);
    private Scalar upperObstacleThreshold = new Scalar(130, 50, 255);
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