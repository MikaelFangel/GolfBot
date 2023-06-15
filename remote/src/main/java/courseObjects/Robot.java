package courseObjects;

import org.opencv.core.Point;
import vision.math.Geometry;

import java.util.Properties;

public class Robot {
    private Point center, front;
    private double angle;
    public final double length, width, height;

    private int ballsInMagazine = 0;
    private final int magazineSize;

    public Robot(Point center, Point front) {
        this(); // Call default constructor

        this.center = center;
        this.front = front;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }

    public Robot() {
        // Measurements are in CM
        Properties configProp = configs.GlobalConfig.getConfigProperties();
        this.length = Double.parseDouble(configProp.getProperty("robotLength"));
        this.width = Double.parseDouble(configProp.getProperty("robotWidth"));
        this.height = Double.parseDouble(configProp.getProperty("robotHeight"));
        this.magazineSize = Integer.parseInt(configProp.getProperty("magazineSize"));
    }

    ;

    public synchronized Point getCenter() {
        return center;
    }

    public synchronized void setFrontAndCenter(Point center, Point front) {
        this.front = front;
        this.center = center;
        this.angle = Geometry.angleBetweenTwoPoints(center.x, center.y, front.x, front.y);
    }

    public synchronized Point getFront() {
        return front;
    }

    public synchronized void setAngle(double angle) {
        this.angle = angle;
    }

    public synchronized double getAngle() {
        return angle;
    }

    public synchronized void addOrRemoveNumberOfBallsInMagazine(int ballsToAdd) {
        // Not allow below 0
        if (this.ballsInMagazine + ballsToAdd < 0)
            this.ballsInMagazine = 0;

        // Not allow over magazine size
        else if (this.ballsInMagazine + ballsToAdd > this.magazineSize)
            this.ballsInMagazine = this.magazineSize;

        else
            this.ballsInMagazine += ballsToAdd;
    }

    public synchronized void setNumberOfBallsInMagazine(int numberOfBalls) {
        if (numberOfBalls > this.magazineSize)
            this.ballsInMagazine = this.magazineSize;

        else
            this.ballsInMagazine = Math.max(numberOfBalls, 0);
    }

    public synchronized int getNumberOfBallsInMagazine() {
        return this.ballsInMagazine;
    }

    public int getMagazineSize() {
        return this.magazineSize;
    }
}
