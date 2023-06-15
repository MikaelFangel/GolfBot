package routing.Algorithm;

import courseObjects.BallColor;
import courseObjects.Course;
import helperClasses.Pair;
import org.opencv.core.Point;
import routing.Routine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RoutePlanner implements IRoutePlanner {
    List<Routine> path;
    Course course;

    @Override
    public void computeFullRoute(Course course, int numberOfBallsInStorage) {
        this.course = course;
        //TODO: variable that should come from the config file
        int storageSize = 7;
        Point goalPosition = new Point(200,200);

        //creates the graph and using incidentlists to show possible path
        //robot 0
        //balls 1-amountOfWhiteballs
        //orange ball amountOfWhiteBalls + 1 (only if it exsist)
        //Goal amountOfWhiteBalls + 2 - same + number of goal runs needed
        List<graphPoints> vertex = new ArrayList<>();
        vertex.add(new graphPoints(pointTypes.ROBOT,course.getRobot().getCenter()));
        course.getBalls().forEach(ball -> {
                    vertex.add(new graphPoints(
                            ball.getColor() == BallColor.WHITE ? pointTypes.WHITEBALL : pointTypes.ORANCEBALL,
                            ball.getCenter()
                    ));
                }
        );

        //adds the goals
        int numberOfGoalRuns = course.getBalls().size() > storageSize - numberOfBallsInStorage ? 2 : 1;
        for(int i = 0; i < numberOfGoalRuns; i++){
            vertex.add(new graphPoints(pointTypes.GOAL,goalPosition));
        }

        //creating an array of linkedlister, acting as a incidentlist
        ArrayList<LinkedList<Pair<graphPoints,Integer>>> incidentList = new ArrayList<>();

        //ensures that everyone know their own position in the incident list
        for (int i = 0; i < vertex.size(); i++){
            vertex.get(i).indexInIncidentList = i;
        }

        addWhiteBallToIncidentList(vertex, incidentList);
        addOrangeBallToIncidentList(vertex, incidentList);
        addGoalsToIncidentList(vertex, incidentList, storageSize == numberOfBallsInStorage, numberOfGoalRuns);



    }

    @Override
    public List<Routine> getComputedRoute() {
        return path;
    }

    private void addWhiteBallToIncidentList(
            final List<graphPoints> vertex,
            final ArrayList<LinkedList<Pair<graphPoints, Integer>>> incidentList
    ) {
        List<graphPoints> whiteBalls = vertex.stream().filter(pointTypes -> pointTypes.type.equals(RoutePlanner.pointTypes.WHITEBALL)).toList();
        whiteBalls.forEach(graphPoints -> {
            whiteBalls.forEach(graphPoints1 -> {
                if (graphPoints.equals(graphPoints1)) return;
                incidentList.get(graphPoints.indexInIncidentList).add(new Pair<>(graphPoints1, routePrice(graphPoints.position, graphPoints1.position)));
                incidentList.get(graphPoints1.indexInIncidentList).add(new Pair<>(graphPoints, routePrice(graphPoints.position, graphPoints1.position)));
            });
        });
    }

    private void addOrangeBallToIncidentList(
            final List<graphPoints> vertex,
            final ArrayList<LinkedList<Pair<graphPoints, Integer>>> incidentList
    ){
        //for now we simply add it equal to a whiteball
        vertex.stream()
                .filter(graphPoints -> graphPoints.type == pointTypes.ORANCEBALL)
                .forEach(graphPoints -> {
                    vertex.stream()
                            .filter(graphPoints1 -> graphPoints1.type == pointTypes.WHITEBALL)
                            .forEach(graphPoints1 -> {
                                incidentList.get(graphPoints.indexInIncidentList).add(new Pair<>(graphPoints1, routePrice(graphPoints.position, graphPoints1.position)));
                                incidentList.get(graphPoints1.indexInIncidentList).add(new Pair<>(graphPoints, routePrice(graphPoints.position, graphPoints1.position)));
                            });
                });
    }

    private void addGoalsToIncidentList(
            final List<graphPoints> vertex,
            final ArrayList<LinkedList<Pair<graphPoints, Integer>>> incidentList,
            boolean robotFull,
            int numberOfGoals
    ){
        // if robot is full and there only exsist 1 remaining goal
        if (robotFull && numberOfGoals == 1){
            incidentList.get(0).add(new Pair<>(vertex.get(1), 0));
            return;
        }
        // if there is still balls and maybe even multiple goals left, then we make a connection between all balls and all goals
        vertex.stream()
                .filter(graphPoints -> graphPoints.type.equals(pointTypes.GOAL))
                .forEach(graphPoints -> {
                    vertex.stream()
                            .filter(graphPoints1 -> graphPoints1.type.equals(pointTypes.WHITEBALL) || graphPoints1.type.equals(pointTypes.ORANCEBALL))
                            .forEach(graphPoints1 -> incidentList.get(graphPoints1.indexInIncidentList).add(new Pair<>(graphPoints,routePrice(graphPoints.position ,graphPoints1.position))));
                });

        //if there is more then one goal run left, then all but the last need to point out to all balls aswell
        if (numberOfGoals < 2) return;
        List<graphPoints> goals = new ArrayList<>();
        vertex.stream()
                .filter(graphPoints -> graphPoints.type.equals(pointTypes.GOAL))
                .forEach(goals::add);
        for (int i = 0; i < goals.size() - 1; i++) {
            for (graphPoints gp : vertex.stream().filter(gp -> gp.type.equals(pointTypes.WHITEBALL)).toList()){
                incidentList.get(goals.get(i).indexInIncidentList).add(new Pair<>(gp,routePrice(goals.get(i).position,gp.position)));
            }
        }
    }

    private int routePrice(Point p1, Point p2){
        return 0;
    }

    private class graphPoints{
        int indexInIncidentList = -1;
        pointTypes type;
        boolean visited;
        Point position;

        public graphPoints(pointTypes type, Point position){
            this.type = type;
            this.position = position;
            visited = false;
        }
    }

    private enum pointTypes{
        WHITEBALL, ORANCEBALL, ROBOT, GOAL
    }
}
