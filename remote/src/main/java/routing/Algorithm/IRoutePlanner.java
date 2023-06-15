package routing.Algorithm;

import courseObjects.Course;
import org.opencv.core.Point;
import routing.Routine;

import java.util.List;

public interface IRoutePlanner {
    void computeFullRoute(Course course, int numberOfBallsInStorage);
    List<Routine> getComputedRoute();
}
