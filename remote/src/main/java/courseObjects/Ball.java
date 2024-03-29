package courseObjects;

import org.opencv.core.Point;
import vision.BallPickupStrategy;

public class Ball {
    private Point center;
    private BallColor color;
    private BallPickupStrategy strategy;
    private final double radius;

    public Ball(Point center, BallColor color, BallPickupStrategy strategy) {
        this(); // Call default constructor

        this.center = center;
        this.color = color;
        this.strategy = strategy;
    }

    public Ball(){
        this.radius = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("ballRadius")); // CM
    }

    public BallColor getColor() {
        return color;
    }

    public void setColor(BallColor color) {
        this.color = color;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public BallPickupStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(BallPickupStrategy strategy) {
        this.strategy = strategy;
    }

    public double getRadius() {
        return this.radius;
    }
}
