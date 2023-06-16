import courseObjects.Cross;
import math.Geometry;
import org.opencv.core.Point;

public class test {
    public static void main(String[] args) {

        // Calculate number of times to rotate 90 degrees.
        double angleToBall = 135;
        double angleToMeasurePoint = 270;

        double diffAngle = angleToBall - angleToMeasurePoint;
        double numRotations = (int) (diffAngle / 90);
        int direction = (diffAngle >= 0) ? 1 : -1;

        double projectionAngle = angleToMeasurePoint + (direction * 45) + (90 * numRotations);
    }
}
