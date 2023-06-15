package routing.Algorithm;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;
import routing.Routine;

import java.util.List;

public interface IRoutePlanner {
    void computeFullRoute(Course course, int numberOfBallsInStorage);
    List<Ball> getComputedRoute();
}
