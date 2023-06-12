package courseObjects;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class Cross {
    private List<Point> endPoints = new ArrayList<>();
    private Point middle;
    private Point measurePoint;
    private final double longestSide;
    private final double shortestSide;

    public Cross() {
        // Measurements are in CM
        this.longestSide = 20.1;
        this.shortestSide = 3.1;
    }

    public List<Point> getEndPoints() {
        return endPoints;
    }

    public synchronized void setEndPoints(List<Point> endPoints) {
        if (endPoints.size() != 12) return;
        this.endPoints = endPoints;
    }

    @Override
    public String toString() {
        return "Cross{" +
                "endPoints=" + endPoints +
                '}';
    }

    public synchronized Point getMiddle() {
        return middle;
    }

    public synchronized void setMiddle(Point middle) {
        this.middle = middle;
    }

    /**
     * @return The point that is used to measure the position of the cross
     */
    public synchronized Point getMeasurePoint() {
        return measurePoint;
    }

    /**
     * Set point used to measure the position of the cross
     *
     * @param measurePoint Point to be measured from
     */
    public synchronized void setMeasurePoint(Point measurePoint) {
        this.measurePoint = measurePoint;
    }

    public synchronized double getLongestSide() {
        return longestSide;
    }

    public synchronized double getShortestSide() {
        return shortestSide;
    }
}
