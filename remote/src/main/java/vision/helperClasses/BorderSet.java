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
