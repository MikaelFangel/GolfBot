package vision.math;

import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Geometry {
    public static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double angleBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        double xDiff = x2 - x1;
        double yDiff = y2 - y1;

        double angle = Math.atan((yDiff) / (xDiff)) * 180 / Math.PI;

        // Add depending on quadrant
        if (xDiff <= 0 && yDiff >= 0) // Quadrant 2
            angle += 180;
        else if (xDiff <= 0 && yDiff <= 0) // Quadrant 3
            angle += 180;
        else if (xDiff >= 0 && yDiff <= 0) // Quadrant 4
            angle += 360;

        return angle;
    }

    /**
     * @param heightCamera Height from floor to camera in cm
     * @param heightObject Height from floor to measure point on object in cm
     * @param distanceToCamera distance from object to camera, in a perpendicular line to the camera, in cm.
     * @return the distance in centimeter from the camera to the object.
     */
    public static double objectActualPosition(double heightCamera, double heightObject, double distanceToCamera){
        double largeTriangleHypotenuse = Math.sqrt(Math.pow(heightCamera, 2) + Math.pow(distanceToCamera, 2));
        if (largeTriangleHypotenuse == 0) return -1; //to prevent division with 0
        //calculate the angel opposite of the height using the sinus relation
        double angelHeight = Math.toDegrees(Math.asin(heightCamera / largeTriangleHypotenuse));
        double smallTriangleGroundAngel = 90 - angelHeight;
        if (Math.sin(smallTriangleGroundAngel) == 0) return -1; //to prevent division with 0
        //finds the length of the small triangle using the sinus relation
        double smallTriangleLength =
                (heightObject*Math.sin(Math.toRadians(smallTriangleGroundAngel))) / Math.sin(Math.toRadians(angelHeight));

        return distanceToCamera - smallTriangleLength;
    }

    /**
     * Generate a circle of points from a specific point.
     * @param center the center of the circle to be drawn
     * @param radius the of the circle to be drawn
     * @param points the amount of points to draw the circle with
     * @return the list of points representing the circle
     */
    public List<Point> generateCircle(Point center, double radius, int points) {
        double angle = 360. / points;

        List<Point> circle = new ArrayList<>();
        for (double i = 0; i < 360; i += angle) {
            circle.add(new Point(center.x + radius * Math.cos(i), center.y +radius * Math.sin(i)));
        }
        return circle;
    }
}
