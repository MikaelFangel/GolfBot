package courseObjects;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class Cross {
    private List<Point> endPoints = new ArrayList<>();
    private Point middle;
    private Point measurePoint;

    /**
     * The first endpoint is one of the topmost endpoints, but the rest is consecutively the endPoint to its left on the
     * real world cross
     * @return The list of the 12 endpoints of the cross.
     */
    public List<Point> getEndPoints() {
        return this.endPoints;
    }

    public synchronized void setEndPoints(List<Point> endPoints) {
        if (endPoints.size() != 12) return;
        this.endPoints = endPoints;
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
        return Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("crossSideLong"));
    }

    public synchronized double getShortestSide() {
        return Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("crossSideShort"));
    }

    public double getHeight() {
        return Double.parseDouble(configs.GlobalConfig.getConfigProperties().getProperty("crossHeight"));
    }
}
