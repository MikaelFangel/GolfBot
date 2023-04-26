package vision;

public class Calculations {
    public static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double angleBetweenTwoPoints(double x1, double y1, double x2, double y2){
        double angle = Math.atan( (y2-y1) / (x2-x1)) * 180/Math.PI;

        // Add depending on quadrant
        if (x2 - x1 <= 0 && y2 - y1 >= 0) // Quadrant 2
            angle += 180;
        else if (x2 - x1 <= 0 && y2 - y1 <= 0) // Quadrant 3
            angle += 180;
        else if (x2 - x1 >= 0 && y2 - y1 <= 0) // Quadrant 4
            angle += 360;

        return angle;
    }
}
