package courseObjects;

import org.opencv.core.Point;
public class Border {
    public double height = 7; // In cm
    private Point topLeft, topRight, bottomLeft, bottomRight;


    public Border(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public synchronized Point getBottomRight() {
        return bottomRight;
    }

    public synchronized Point getBottomLeft() {
        return bottomLeft;
    }

    public synchronized Point getTopRight() {
        return topRight;
    }

    public synchronized Point getTopLeft() {
        return topLeft;
    }

    public synchronized void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public synchronized void setBottomLeft(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public synchronized void setTopRight(Point topRight) {
        this.topRight = topRight;
    }

    public synchronized void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public synchronized Point[] getCornersAsArray() {
        return new Point[] {topLeft, topRight, bottomLeft, bottomRight};
    }
}
