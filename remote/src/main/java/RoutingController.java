import courseObjects.Course;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class RoutingController {
    private Course course;
    private List<Route> fullRoute = new ArrayList<Route>();
    private Route currentRoute;

    //
    public void driveRoutes () {

    }

    //
    public void planRoute(Point from, Point to) {
        //if ( /*TODO*/ ) {

        //}
    }

    // Clear planned routes
    public void clearFullRoute() {
        fullRoute.clear();
    }

    // Stop ongoing route
    public void stopCurrentRoute() {

    }
}
