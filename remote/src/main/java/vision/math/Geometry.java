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

    public static double objectActualPosition(double heightCamera, double heightObject, double distanceToCamera){
        double largeTriangle_hypotenuse = Math.sqrt(Math.pow(heightCamera,2)+Math.pow(distanceToCamera,2));
        if (largeTriangle_hypotenuse == 0) return -1;
        double angelHeight = Math.toDegrees(Math.asin(heightCamera/largeTriangle_hypotenuse));
        double smallTriangle_height = heightCamera - heightObject;
        double smallTriangle_groundAngel = 90 - angelHeight;
        if (Math.sin(smallTriangle_groundAngel) == 0) return -1;
        double test = Math.sin(Math.toRadians(smallTriangle_groundAngel));
        double smallTriangle_length = (smallTriangle_height*Math.sin(Math.toRadians(smallTriangle_groundAngel)))/Math.sin(Math.toRadians(angelHeight));

        return distanceToCamera-smallTriangle_length;
    }
}
