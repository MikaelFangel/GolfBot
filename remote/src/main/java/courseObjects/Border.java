package courseObjects;

import org.opencv.core.Point;

public class Border {
    public double height;
    private Point topLeft, topRight, bottomLeft, bottomRight;
    private SmallGoalPos smallGoalPos;

    public Border(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        this(); // Call default constructor

        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public Border() {
        this.height = Integer.parseInt(configs.GlobalConfig.getConfigProperties().getProperty("borderHeight"));
    }

    public synchronized Point getBottomRight() {
        return bottomRight;
    }

    public synchronized void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public synchronized Point getBottomLeft() {
        return bottomLeft;
    }

    public synchronized void setBottomLeft(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public synchronized Point getTopRight() {
        return topRight;
    }

    public synchronized void setTopRight(Point topRight) {
        this.topRight = topRight;
    }

    public synchronized Point getTopLeft() {
        return topLeft;
    }

    public synchronized void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public synchronized Point[] getCornersAsArray() {
        return new Point[]{topLeft, topRight, bottomLeft, bottomRight};
    }

    public void setSmallGoalPos(SmallGoalPos smallGoalPos) {
        this.smallGoalPos = smallGoalPos;
    }

    /**
     * @return The middle point of the small goal
     */
    public synchronized Point getSmallGoalPoint() {
        switch (this.smallGoalPos) {
            case LEFT -> {
                return new Point(
                        (this.topLeft.x + this.bottomLeft.x) / 2,
                        (this.topLeft.y + this.bottomLeft.y) / 2
                );
            }
            case RIGHT -> {
                return new Point(
                        (this.topRight.x + this.bottomRight.x) / 2,
                        (this.topRight.y + this.bottomRight.y) / 2
                );
            }
            default -> {
                return null;
            }
        }
    }
}
