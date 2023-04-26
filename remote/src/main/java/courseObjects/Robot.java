package courseObjects;

import org.opencv.core.Point;

public class Robot {
    Point centerMarker, rotationMarker;
    final double length = 19.0, width = 17.0, height = 18.0;

    public Robot(Point centerMarker, Point rotationMarker){
        this.centerMarker = centerMarker;
        this.rotationMarker = rotationMarker;
    }
}
