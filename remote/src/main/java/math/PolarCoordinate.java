package math;

public class PolarCoordinate {
    private double angel, distance;
    public PolarCoordinate(double distance, double angel){
        this.angel = angel;
        this.distance = distance;
    }

    public double getAngel() {
        return angel;
    }

    public double getDistance() {
        return distance;
    }
}
