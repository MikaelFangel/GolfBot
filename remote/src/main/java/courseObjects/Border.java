package courseObjects;

import org.opencv.core.Point;
public class Border {
    public double height = 7; // In cm
    final private Point topLeft, topRight, bottomLeft, bottomRight;


    public Border(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public Point getTopRight() {
        return topRight;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public Point[] getCornersAsArray() {
        return new Point[] {topLeft, topRight, bottomLeft, bottomRight};
    }
}
