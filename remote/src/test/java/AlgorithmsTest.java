import courseObjects.Course;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;
import vision.Algorithms;

public class AlgorithmsTest {
    Course course;

    @BeforeEach
    public void setup(){
        course = new Course(200);
    }

    @Test
    public void correctedCoordinatesOfObjectTest1(){
        Point  newPoint = Algorithms.correctedCoordinatesOfObject(
                new Point(20,20),
                new Point(150, 150),
                40,
                165
        );
        Assertions.assertEquals((int) newPoint.x, 51);
        Assertions.assertEquals((int) newPoint.y, 51);
    }

    @Test
    public void correctedCoordinatesOfObjectTest2(){
        Point  newPoint = Algorithms.correctedCoordinatesOfObject(
                new Point(200,200),
                new Point(150, 150),
                40,
                165
        );
        Assertions.assertEquals((int) newPoint.x, 187);
        Assertions.assertEquals((int) newPoint.y, 187);
    }
}