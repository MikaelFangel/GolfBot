package routing.Algorithm;

import courseObjects.Course;
import routing.RoutingController;

public interface IRoutePlanner {
    void computeFullRoute(Course course, int numberOfBallsInStorage);
    void getComputedRoute(RoutingController rc);
}
