package courseObjects;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class Cross {
    private List<Point> endPoints = new ArrayList<>();

    public List<Point> getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(List<Point> endPoints) {
        this.endPoints = endPoints;
    }

    @Override
    public String toString() {
        return "Cross{" +
                "endPoints=" + endPoints +
                '}';
    }
}
