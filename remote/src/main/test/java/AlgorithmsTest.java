import courseObjects.Ball;
import courseObjects.BallColor;
import courseObjects.Robot;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;
import vision.Algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlgorithmsTest {

    @Test
    void findRobotShortestAngleToBallQ1Test() {
        // value of the robotFrontX is not important for the calculation of the robot angle so for now 10 is choosen
        int robotCenterX = 0;
        int robotCenterY = 0;
        int robotFrontX = 10;
        int robotFrontY = 0;

        int ballCenterX = 10;
        int ballCenterY = 10;

        double shortestAngleToCalculate = 45;

        Robot robot = new Robot(new Point(robotCenterX,robotCenterY), new Point(robotFrontX, robotFrontY));

        Ball ball = new Ball(new Point(ballCenterX,ballCenterY), BallColor.WHITE);

        double calculatedAngle = Algorithms.findRobotShortestAngleToBall(robot, ball);

        assertEquals(shortestAngleToCalculate, calculatedAngle);
    }
    @Test
    void findRobotShortestAngleToBallQ2Test() {
        // value of the robotFrontY is not important for the calculation of the robot angle so for now 10 is choosen
        int robotCenterX = 0;
        int robotCenterY = 0;
        int robotFrontX = 10;
        int robotFrontY = 0;

        int ballCenterX = -10;
        int ballCenterY = 10;

        double shortestAngleToCalculate = 135;

        Robot robot = new Robot(new Point(robotCenterX,robotCenterY), new Point(robotFrontX, robotFrontY));

        Ball ball = new Ball(new Point(ballCenterX,ballCenterY), BallColor.WHITE);

        double calculatedAngle = Algorithms.findRobotShortestAngleToBall(robot, ball);

        assertEquals(shortestAngleToCalculate, calculatedAngle);
    }

    @Test
    void findRobotShortestAngleToBallQ3Test() {
        // value of the robotFrontY is not important for the calculation of the robot angle so for now 10 is choosen
        int robotCenterX = 0;
        int robotCenterY = 0;
        int robotFrontX = 10;
        int robotFrontY = 0;

        int ballCenterX = -10;
        int ballCenterY = -10;

        double shortestAngleToCalculate = -135;

        Robot robot = new Robot(new Point(robotCenterX,robotCenterY), new Point(robotFrontX, robotFrontY));

        Ball ball = new Ball(new Point(ballCenterX,ballCenterY), BallColor.WHITE);

        double calculatedAngle = Algorithms.findRobotShortestAngleToBall(robot, ball);

        assertEquals(shortestAngleToCalculate, calculatedAngle);
    }

    @Test
    void findRobotShortestAngleToBallQ4Test() {
        // value of the robotFrontY is not important for the calculation of the robot angle so for now 10 is choosen
        int robotCenterX = 0;
        int robotCenterY = 0;
        int robotFrontX = 10;
        int robotFrontY = 0;

        int ballCenterX = 10;
        int ballCenterY = -10;

        double shortestAngleToCalculate = -45;

        Robot robot = new Robot(new Point(robotCenterX,robotCenterY), new Point(robotFrontX, robotFrontY));

        Ball ball = new Ball(new Point(ballCenterX,ballCenterY), BallColor.WHITE);

        double calculatedAngle = Algorithms.findRobotShortestAngleToBall(robot, ball);

        assertEquals(shortestAngleToCalculate, calculatedAngle);
    }

    @Test
    void findRobotShortestAngleToBallZeroTest() {
        // value of the robotFrontY is not important for the calculation of the robot angle so for now 10 is choosen
        int robotCenterX = 0;
        int robotCenterY = 0;
        int robotFrontX = 10;
        int robotFrontY = 0;

        int ballCenterX = 10;
        int ballCenterY = 0;

        double shortestAngleToCalculate = 0;

        Robot robot = new Robot(new Point(robotCenterX,robotCenterY), new Point(robotFrontX, robotFrontY));

        Ball ball = new Ball(new Point(ballCenterX,ballCenterY), BallColor.WHITE);

        double calculatedAngle = Algorithms.findRobotShortestAngleToBall(robot, ball);

        assertEquals(shortestAngleToCalculate, calculatedAngle);
    }

    @Test
    void findRobotShortestAngleToBall180Test() {
        // value of the robotFrontY is not important for the calculation of the robot angle so for now 10 is choosen
        int robotCenterX = 0;
        int robotCenterY = 0;
        int robotFrontX = 10;
        int robotFrontY = 0;

        int ballCenterX = -10;
        int ballCenterY = 0;

        double shortestAngleToCalculate = 180;

        Robot robot = new Robot(new Point(robotCenterX,robotCenterY), new Point(robotFrontX, robotFrontY));

        Ball ball = new Ball(new Point(ballCenterX,ballCenterY), BallColor.WHITE);

        double calculatedAngle = Algorithms.findRobotShortestAngleToBall(robot, ball);

        assertEquals(shortestAngleToCalculate, calculatedAngle);
    }
}
