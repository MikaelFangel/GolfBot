package courseObjects;

import org.opencv.core.Point;

public class Border {
    public double height;
    private Point topLeft, topRight, bottomLeft, bottomRight;
    private String smallGoalPos = null;
    private final Point smallGoalMiddlePoint = new Point();

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

    /**
     * @return The point to which 'the rear end of the robot' should arrive before turning toward the goal with an angle
     */
    public synchronized Point getSmallGoalPoint() {
        if (this.topLeft == null || this.topRight == null || this.bottomLeft == null || this.bottomRight == null)
            return null;

        // Set smallGoalPos only once (left or right)
        if (this.smallGoalPos == null)
            this.smallGoalPos = configs.GlobalConfig.getConfigProperties().getProperty("smallGoalPos");

        switch (this.smallGoalPos) {
            case "left" -> {
                this.smallGoalMiddlePoint.x = (this.topLeft.x + this.bottomLeft.x) / 2;
                this.smallGoalMiddlePoint.y = (this.topLeft.y + this.bottomLeft.y) / 2;
            }
            case "right" -> {
                this.smallGoalMiddlePoint.x = (this.topRight.x + this.bottomRight.x) / 2;
                this.smallGoalMiddlePoint.y = (this.topRight.y + this.bottomRight.y) / 2;
            }
        }

        return new Point(this.smallGoalMiddlePoint.x + 50.0, this.smallGoalMiddlePoint.y); //23.0
//        return this.smallGoalMiddlePoint;
    }
}
