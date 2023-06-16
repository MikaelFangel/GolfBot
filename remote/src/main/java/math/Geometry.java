package math;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.Point;

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
    public static List<Point> generateCircle(@NotNull Point center, double radius, int points) throws ArithmeticException {
        if (points < 1)
            throw new ArithmeticException("A circle must consist of at least one point");
        double angle = 360. / points;

        List<Point> circle = new ArrayList<>();
        for (double i = 0; i < 360; i += angle) {
            circle.add(new Point(center.x + radius * Math.cos(i), center.y +radius * Math.sin(i)));
        }
        return circle;
    }

    /**
     * This method checks whether a given line calculated from two points is intersecting a circle.
     * @param from the starting point of the line
     * @param to the ending point of the line
     * @param circleCenter the center point of the circle (eg. course.getCross().getMiddle())
     * @param circleRadius the radius of the circle to check if intersecting  (eg. course.getLongestSide() / 2)
     * @return true if the line is intersecting with the circle
     */
    public static boolean lineIsIntersectingCircle(@NotNull Point from, @NotNull Point to, @NotNull Point circleCenter, double circleRadius) {
        // Calculate the line between two points
        double a = (from.y - to.y) / (from.x - to.x);
        double b = from.y - a * from.x;

        // Calculate the distance from the center of the circle
        double dist = Math.abs(a*circleCenter.x + b - circleCenter.y) / Math.sqrt(Math.pow(a, 2) + 1);
        return dist < circleRadius;
    }

    public static PolarCoordinate cartesianToPolar(Point cartesian){
        return new PolarCoordinate(
                Math.sqrt(Math.pow(cartesian.x, 2) + Math.pow(cartesian.y, 2)),
                Math.toDegrees(Math.atan2(cartesian.y, cartesian.x))
        );
    }

    public static Point polarToCartesian(PolarCoordinate polar){
        return new Point(
                polar.getDistance() * Math.cos(Math.toRadians(polar.getAngel())),
                polar.getDistance() * Math.sin(Math.toRadians(polar.getAngel()))
        );
    }
}
