package courseObjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;

class RobotTest {

    Robot robot;

    @BeforeEach
    void setup() {
        robot = new Robot(new Point(0,0), new Point(10, 10));

        // Assert empty at start
        Assertions.assertEquals(robot.getNumberOfBallsInMagazine(), 0);
    }

    @Test
    void addBallsInMagazine() {
        // Assert addition
        robot.addOrRemoveNumberOfBallsInMagazine(1);
        Assertions.assertEquals(robot.getNumberOfBallsInMagazine(), 1);

        // Assert Addition Over limit
        robot.addOrRemoveNumberOfBallsInMagazine(robot.getMagazineSize());
        Assertions.assertEquals(robot.getMagazineSize(), robot.getNumberOfBallsInMagazine());

        // Assert Removing Balls
        robot.addOrRemoveNumberOfBallsInMagazine(-1);
        Assertions.assertEquals(robot.getMagazineSize()-1, robot.getNumberOfBallsInMagazine());

        // Remove beyond 0
        robot.addOrRemoveNumberOfBallsInMagazine(-robot.getMagazineSize());
        Assertions.assertEquals(0, robot.getNumberOfBallsInMagazine());
    }

    @Test
    void setBallsInMagazine() {
        // Assert set within magazine limit
        int midOfMagazine = (int) Math.ceil( robot.getMagazineSize() / 2 );
        robot.setNumberOfBallsInMagazine(midOfMagazine);
        Assertions.assertEquals(midOfMagazine, robot.getNumberOfBallsInMagazine());

        // Assert beyond limit
        robot.setNumberOfBallsInMagazine(robot.getMagazineSize() + 2);
        Assertions.assertEquals(robot.getMagazineSize(), robot.getNumberOfBallsInMagazine());

        // Assert below 0
        robot.setNumberOfBallsInMagazine(-3);
        Assertions.assertEquals(0, robot.getNumberOfBallsInMagazine());
    }
}