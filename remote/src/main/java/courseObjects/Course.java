package courseObjects;

import org.opencv.core.Point;

public class Course {
    final double length = 167.0, width = 122.0;
    public Point topLeft, topRight, bottomLeft, bottomRight;
    public Ball[] balls;
}
