package courseObjects;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cross {
    private List<Point> endPoints = new ArrayList<>();
    private Point middle;
    private Point measurePoint;
    private final double longestSide = 20.1; // CM
    private final double shortestSide = 3.1; // CM

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
