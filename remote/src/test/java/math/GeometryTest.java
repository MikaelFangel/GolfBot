package math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vision.math.Geometry;

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
}
