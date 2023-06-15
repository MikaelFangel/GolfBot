package routing.Algorithm;

import courseObjects.Ball;
import courseObjects.Course;
import org.opencv.core.Point;
import routing.Routine;

import java.util.ArrayList;
import java.util.List;

public class HamiltonianRoute implements IRoutePlanner{
    List<Routine> plan;
    Course course;
    @Override
    public void computeFullRoute(Course course, int numberOfBallsInStorage) {
        //TODO: get these from the right place...
        Point goal = new Point(200,200);

        //initialization, but not pushing to the actual one, as it might be used while recomputing route
        List<Routine> planning = new ArrayList<>();



        //saving the course in class for now, until i know how many private methods need it aswell
        //TODO: remove?
        this.course = course;

        List<Vertex> vertexs = new ArrayList<>();

        //placing robot as the first element
        vertexs.add(new Vertex(course.getRobot().getCenter(), Type.ROBOT));

        //placing the goal as the second element
        vertexs.add(new Vertex(goal, Type.GOAL));

        //placing all balls
        List<Ball> balls = course.getBalls();

        //if there is no balls, the robot should go to the goal
        if (balls.isEmpty()){
            plan.add(new R)

        }


    }

    @Override
    public List<Routine> getComputedRoute() {
        return null;
    }

    //used for data needed in each graph note
    private class Vertex{
        Point position;
        Type type;

        Vertex(Point position, Type type){
            position = position;
            this.type = type;
        }
    }

    enum Type{
        ROBOT, BALL, GOAL
    }
}
