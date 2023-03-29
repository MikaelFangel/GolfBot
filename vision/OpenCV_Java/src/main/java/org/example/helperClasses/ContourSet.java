package org.example.helperClasses;

import org.opencv.core.MatOfPoint;

public class ContourSet {
    public Double area;
    public MatOfPoint contour;
    public ContourSet(Double area, MatOfPoint contour) {
        this.area = area;
        this.contour = contour;
    }
}