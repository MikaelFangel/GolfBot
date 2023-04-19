package vision.helperClasses;

import org.opencv.core.MatOfPoint;

/**
 * Helper class to help sort Contours using their area as key
 */
public class ContourSet {
    public Double area;
    public MatOfPoint contour;
    public ContourSet(Double area, MatOfPoint contour) {
        this.area = area;
        this.contour = contour;
    }
}