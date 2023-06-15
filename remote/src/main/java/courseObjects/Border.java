package courseObjects;

import org.opencv.core.Point;

public class Border {
    public double height;
    private Point topLeft, topRight, bottomLeft, bottomRight;
    private String smallGoalPos;
    private final Point smallGoalMiddlePoint = new Point();
    private final Point smallGoalDestPoint = new Point();

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
     * Update the small goal point before using it
     */
    private synchronized boolean updateSmallGoalPoint() {
        // Can't calculate point without these
        if (this.topLeft == null || this.topRight == null || this.bottomLeft == null || this.bottomRight == null)
            return false;

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

        return true;
    }

    /**
     * @return The middle point of the small goal
     */
    public synchronized Point getSmallGoalMiddlePoint() {
        if (!updateSmallGoalPoint())
            return null;
        return this.smallGoalMiddlePoint;
    }

    /**
     * NB! Always call getSmallGoalPoint before this one to makes sure x and y of smallGoalMiddlePoint is set correctly
     * @return The point to which 'the rear end of the robot' should arrive before turning toward the goal with an angle
     */
    public synchronized Point getSmalGoalDestPoint() {
        if (!updateSmallGoalPoint())
            return null;

        // The distance between the goal and the point where the rear end of the robot should stop
        int offset = 25;

        this.smallGoalDestPoint.x = switch (this.smallGoalPos) {
            case "left" -> this.smallGoalMiddlePoint.x + offset;
            case "right" -> this.smallGoalMiddlePoint.x - offset;
            default -> this.smallGoalMiddlePoint.x;
        };

        this.smallGoalDestPoint.y = this.smallGoalMiddlePoint.y;

        return this.smallGoalDestPoint;
    }
}
