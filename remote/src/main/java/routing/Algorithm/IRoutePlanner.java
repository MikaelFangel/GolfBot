package routing.Algorithm;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;
import routing.Routine;
import routing.RoutingController;

import java.util.List;

public interface IRoutePlanner {
    void computeFullRoute(Course course, int numberOfBallsInStorage);
    void getComputedRoute(RoutingController rc);
}
