package vision.helperClasses;

import org.opencv.core.MatOfPoint;

/**
 * Helper class to help sort Contours using their area as key
 */
public class ContourSet {
    private Double area;
    private MatOfPoint contour;
    public ContourSet(Double area, MatOfPoint contour) {
        this.area = area;
        this.contour = contour;
    }

    public void setContour(MatOfPoint contour) {
        this.contour = contour;
    }

    public MatOfPoint getContour() {
        return contour;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public Double getArea() {
        return area;
    }
}