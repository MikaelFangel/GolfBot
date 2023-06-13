package math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;

import java.text.DecimalFormat;

public class GeometryTest {
    DecimalFormat decimal = new DecimalFormat("#.00");
    @Test
    public void objectActualPosition1(){
        double result = Geometry.objectActualPosition(120,20,40);
        Assertions.assertEquals("6.67", decimal.format(result));
    }

    @Test
    public void objectActualPosition2(){
        double result = Geometry.objectActualPosition(240,40,80);
        Assertions.assertEquals("13.33",decimal.format(result));
    }

    @Test
    public void polarToCartesianTest1(){
        Point cartesian = Geometry.polarToCartesian(new PolarCoordinate(13.0,22.6));
        Assertions.assertEquals("12.00", decimal.format(cartesian.x));
        Assertions.assertEquals("5.00", decimal.format(cartesian.y));
    }

    @Test
    public void cartesianToPolarTest1(){
        PolarCoordinate polar = Geometry.cartesianToPolar(new Point(12,5));
        Assertions.assertEquals(13, polar.getDistance());
        Assertions.assertEquals("22.62", decimal.format(polar.getAngel()));
    }
}
