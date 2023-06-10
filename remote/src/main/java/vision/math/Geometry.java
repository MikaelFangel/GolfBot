package vision.math;

import org.opencv.core.Point;

public class Geometry {

    public static double distanceBetweenTwoPoints(Point p1, Point p2) {
        return distanceBetweenTwoPoints(p1.x, p1.y, p2.x, p2.y);
    }
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
     * @param heightCamera Height from floor to camera
     * @param heightObject Height from floor to measure point on object
     * @param distanceToCamera distance from object to camera, in a perpendicular line to the camera.
     * @return the distance in centimeter from the camera to the object.
     */
    public static double objectActualPosition(double heightCamera, double heightObject, double distanceToCamera){
        double largeTriangleHypotenuse = Math.sqrt(Math.pow(heightCamera,2)+Math.pow(distanceToCamera,2));
        if (largeTriangleHypotenuse == 0) return -1; //to prevent division with 0
        //calculate the angel opposite of the height using the sinus relation
        double angelHeight = Math.toDegrees(Math.asin(heightCamera/largeTriangleHypotenuse));
        double smallTriangleGroundAngel = 90 - angelHeight;
        if (Math.sin(smallTriangleGroundAngel) == 0) return -1; //to prevent division with 0
        //finds the length of the small triangle using the sinus relation
        double smallTriangleLength =
                (heightObject*Math.sin(Math.toRadians(smallTriangleGroundAngel)))/Math.sin(Math.toRadians(angelHeight));

        return distanceToCamera-smallTriangleLength;
    }
}
