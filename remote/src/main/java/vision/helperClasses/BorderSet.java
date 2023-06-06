package vision.helperClasses;

import org.opencv.core.Point;

public class BorderSet {
    private Point[] correctCoords;
    private Point origin;

    public BorderSet(Point[] correctCoords, Point origin) {
        this.correctCoords = correctCoords;
        this.origin = origin;
    }

    public void setCorrectCoords(Point[] correctCoords) {
        this.correctCoords = correctCoords;
    }

    /**
     * @return list of length 4 in order {TopLeft, TopRight, BottomLeft, BottomRight}
     */
    public Point[] getCorrectCoords() {
        return correctCoords;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public Point getOrigin() {
        return origin;
    }
}
