package routing.Algorithm;

import courseObjects.Course;
import org.opencv.core.Point;
import routing.Route;

import java.util.List;

public interface IRoutePlanner {
    void computeFullRoute(int numberOfBallsInStorage);
    List<Route> getComputedRoute();
}
