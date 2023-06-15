package courseObjects;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class Cross {
    private Point middle;
    private Point measurePoint;

    public Cross() {
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
