package courseObjects;

import org.opencv.core.Point;

public class Border {
    private Point topLeft, topRight, bottomLeft, bottomRight;

    public Border(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight){
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public void setTopRight(Point topRight) {
        this.topRight = topRight;
    }

    public Point getTopRight() {
        return topRight;
    }

    public void setBottomLeft(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public Point getBottomRight() {
        return bottomRight;
    }
}
