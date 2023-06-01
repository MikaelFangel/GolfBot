package vision.math;

public class Geometry {
    public static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double angleBetweenTwoPoints(double x1, double y1, double x2, double y2){
        double xDiff = x2 - x1;
        double yDiff = y2 - y1;

        double angle = Math.atan( (yDiff) / (xDiff)) * 180/Math.PI;

        // Add depending on quadrant
        if (xDiff <= 0 && yDiff >= 0) // Quadrant 2
            angle += 180;
        else if (xDiff <= 0 && yDiff <= 0) // Quadrant 3
            angle += 180;
        else if (xDiff >= 0 && yDiff <= 0) // Quadrant 4
            angle += 360;

        return angle;
    }
}
