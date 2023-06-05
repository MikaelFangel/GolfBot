import courseObjects.Course;
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
                40,
                course,
                2
        );
        System.out.println(newPoint.toString());
    }
}
