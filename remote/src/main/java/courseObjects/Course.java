package courseObjects;

import org.opencv.core.Point;

public  class Course {
    public final double length = 167.0, width = 122.0;

    private Point topLeft, topRight, bottomLeft, bottomRight;
    private Ball[] balls;
    private Robot robot;


    // Getters and setters
    public synchronized Point getTopLeft() {
        return topLeft;
    }

    public synchronized void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public synchronized Point getTopRight() {
        return topRight;
    }

    public synchronized void setTopRight(Point topRight) {
        this.topRight = topRight;
    }

    public synchronized Point getBottomLeft() {
        return bottomLeft;
    }

    public synchronized void setBottomLeft(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public synchronized Point getBottomRight() {
        return bottomRight;
    }

    public synchronized void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public synchronized Ball[] getBalls() {
        return balls;
    }

    public synchronized void setBalls(Ball[] balls) {
        this.balls = balls;
    }

    public synchronized Robot getRobot() {
        return robot;
    }

    public synchronized void setRobot(Robot robot) {
        this.robot = robot;
    }
}
