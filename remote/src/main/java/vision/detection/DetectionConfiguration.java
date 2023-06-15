package vision.detection;

import org.opencv.core.Scalar;

public class DetectionConfiguration {
    private static DetectionConfiguration single_instance = null;

    // Balls
    private final Scalar lowerWhiteBallThreshold;
    private final Scalar upperWhiteBallThreshold;
    private final Scalar lowerOrangeBallThreshold;
    private final Scalar upperOrangeBallThreshold;
    private final int lowerBallSize;
    private final int upperBallSize;

    private final int ballDp;
    private final int ballMinDist;
    private final int ballParam1;
    private final int ballParam2;

    // Robot (Blue)
    private final Scalar lowerRobotThreshold;
    private final Scalar upperRobotThreshold;
    private final int lowerRobotSize;
    private final int upperRobotSize;

    // Obstacles (Red)
    private final Scalar lowerObstacleThreshold;
    private final Scalar upperObstacleThreshold;
    private final double lowerBorderSize;
    private final double lowerCrossSize;
    private final double upperCrossSize;

    private DetectionConfiguration() {
        // Balls
        this.lowerWhiteBallThreshold = new Scalar(105, 105, 105);
        this.upperWhiteBallThreshold = new Scalar(230, 230, 230);
        this.lowerOrangeBallThreshold = new Scalar(0, 40, 130);
        this.upperOrangeBallThreshold = new Scalar(20, 170, 255);

        this.lowerBallSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballSizeLower"));
        this.upperBallSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballSizeUpper"));
        this.ballDp = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballDp"));
        this.ballMinDist = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballMinDist"));
        this.ballParam1 = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballParam1"));
        this.ballParam2 = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballParam2"));

        // Robot
        this.lowerRobotThreshold = new Scalar(130, 70, 0);
        this.upperRobotThreshold = new Scalar(250, 120, 100);
        this.lowerRobotSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("robotSizeLower"));
        this.upperRobotSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("robotSizeUpper"));

        // Obstacle
        this.lowerObstacleThreshold = new Scalar(0, 0, 100);
        this.upperObstacleThreshold = new Scalar(60, 60, 255);
        this.lowerBorderSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("borderSizeLower"));
        this.lowerCrossSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("crossSizeLower"));
        this.upperCrossSize = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("crossSizeUpper"));
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

    public int getBallDp() {
        return ballDp;
    }

    public int getBallMinDist() {
        return ballMinDist;
    }

    public int getBallParam1() {
        return ballParam1;
    }

    public int getBallParam2() {
        return ballParam2;
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