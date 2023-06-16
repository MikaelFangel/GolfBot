package routing.Algorithm;

import courseObjects.Ball;
import courseObjects.BallColor;
import courseObjects.Course;
import courseObjects.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;
import routing.RoutingController;
import vision.BallPickupStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HamiltonianRouteTest {
    IRoutePlanner routePlanner;

    @BeforeEach
    void setUp() {
        routePlanner = new HamiltonianRoute();
    }

    @Test
    void getComputedRoute() {
        Course course = new Course();
        Robot r =course.getRobot();
        r.setFrontAndCenter(new Point(0,0), new Point(1,1));
        course.getBalls().add(new Ball(new Point(10,10), BallColor.WHITE, BallPickupStrategy.FREE));
        course.getBalls().add(new Ball(new Point(11,10), BallColor.WHITE, BallPickupStrategy.FREE));
        course.getBalls().add(new Ball(new Point(10,18), BallColor.WHITE, BallPickupStrategy.FREE));
        routePlanner.computeFullRoute(course,0);
    }
}