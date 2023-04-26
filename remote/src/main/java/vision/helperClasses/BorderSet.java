package vision.helperClasses;

import org.opencv.core.Point;

public class BorderSet {
    public Point[] correctCoords;
    public Point origin;

    public BorderSet(Point[] correctCoords, Point origin) {
        this.correctCoords = correctCoords;
        this.origin = origin;
    }
}
