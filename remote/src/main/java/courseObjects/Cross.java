package courseObjects;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class Cross {
    private List<Point> endPoints = new ArrayList<>();
    private Point middle;

    public List<Point> getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(List<Point> endPoints) {
        if (endPoints.size() != 12) return;
        this.endPoints = endPoints;
    }

    @Override
    public String toString() {
        return "Cross{" +
                "endPoints=" + endPoints +
                '}';
    }

    public Point getMiddle() {
        return middle;
    }

    public void setMiddle(Point middle) {
        this.middle = middle;
    }
}
