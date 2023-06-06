package vision.helperClasses;

import org.opencv.core.Point;

public class BorderSet {
    private Point[] coords;
    private Point origin;

    public BorderSet(Point[] coords, Point origin) {
        this.coords = coords;
        this.origin = origin;
    }

    public void setCoords(Point[] coords) {
        this.coords = coords;
    }

    /**
     * @return list of length 4 in order {TopLeft, TopRight, BottomLeft, BottomRight}
     */
    public Point[] getCoords() {
        return coords;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public Point getOrigin() {
        return origin;
    }
}
