package org.example.helper;

import org.opencv.core.MatOfPoint;

import java.util.List;

public class ContourSet {
    public Double area;
    public MatOfPoint contour;
    public ContourSet(Double area, MatOfPoint contour) {
        this.area = area;
        this.contour = contour;
    }
}