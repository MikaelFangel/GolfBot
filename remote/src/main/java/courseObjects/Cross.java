package courseObjects;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cross {
    private List<Point> endPoints = new ArrayList<>();
    private Point middle;
    List<Scalar> colors = Arrays.asList(
            new Scalar(255, 255, 255), // White
            new Scalar(255, 0, 0), // Blue
            new Scalar(0, 255, 0), // Green

            new Scalar(0, 0, 255), // Red
            new Scalar(255, 255, 0), // Light Blue
            new Scalar(255, 0, 255), // Magenta

            new Scalar(0, 255, 255), // Yellow
            new Scalar(0, 0, 0), // Black
            new Scalar(19, 69, 139), // Brown

            new Scalar(105, 105, 105), // Grey
            new Scalar(0, 69, 255), // Orange
            new Scalar(212, 255, 127) // Turquoise
    );

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

    /**
     * Distinctively different colors for end endpoints of the cross. This is used for debugging.
     * @return A list of distinctively different colors
     */
    public List<Scalar> getColors() {
        return colors;
    }
}
