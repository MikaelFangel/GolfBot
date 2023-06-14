package courseObjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {

    Robot robot;

    @BeforeEach
    void setup() {
        robot = new Robot(new Point(0,0), new Point(10, 10));

        // Assert empty at start
        Assertions.assertEquals(robot.getBallsInMagazine(), 0);
    }

    @Test
    void addBallsInMagazine() {
        // Assert addition
        robot.addBallsInMagazine(1);
        Assertions.assertEquals(robot.getBallsInMagazine(), 1);

        // Assert Addition Over limit
        robot.addBallsInMagazine(robot.getMagazineSize());
        Assertions.assertEquals(robot.getMagazineSize(), robot.getBallsInMagazine());

        // Assert Removing Balls
        robot.addBallsInMagazine(-1);
        Assertions.assertEquals(robot.getMagazineSize()-1, robot.getBallsInMagazine());

        // Remove beyond 0
        robot.addBallsInMagazine(-robot.getMagazineSize());
        Assertions.assertEquals(0, robot.getBallsInMagazine());
    }

    @Test
    void setBallsInMagazine() {
        // Assert set within magazine limit
        int midOfMagazine = (int) Math.ceil( robot.getMagazineSize() / 2 );
        robot.setBallsInMagazine(midOfMagazine);
        Assertions.assertEquals(midOfMagazine, robot.getBallsInMagazine());

        // Assert beyond limit
        robot.setBallsInMagazine(robot.getMagazineSize() + 2);
        Assertions.assertEquals(robot.getMagazineSize(), robot.getBallsInMagazine());

        // Assert below 0
        robot.setBallsInMagazine(-3);
        Assertions.assertEquals(0, robot.getBallsInMagazine());
    }
}