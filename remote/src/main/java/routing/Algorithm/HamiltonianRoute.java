package routing.Algorithm;

import configs.GlobalConfig;
import courseObjects.Ball;
import courseObjects.BallColor;
import courseObjects.Course;
import helperClasses.Pair;
import math.Geometry;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import routing.Algorithm.UnionFind.IUnionFind;
import routing.Algorithm.UnionFind.QuickFind;
import routing.RoutingController;
import vision.BallPickupStrategy;

import java.util.*;

public class HamiltonianRoute implements IRoutePlanner {
  Point goal;
  Deque<Vertex> plan;
  Course course;

  /**
   * Generates the shortest path, based on a
   *
   * @param course course object where all elements can be found
   * @param numberOfBallsInStorage actual amount of balls in the robots storage
   */
  @Override
  public void computeFullRoute(Course course, int numberOfBallsInStorage) {
    this.goal = course.getBorder().getSmallGoalMiddlePoint();
    this.course = course;
    int maxAmountOfBallsInRobot = Integer.parseInt(GlobalConfig.getConfigProperties().getProperty("magazineSize"));

    List<Vertex> vertices = setupVertex();

    //if there is 1 or fewer vertices, there can't be made any path
    if (vertices.size() <= 1) return;

    List<Edge> edges = setupEdge(vertices);

    //if there is no balls in the robot, we can't visit a corner to begin with
    if (numberOfBallsInStorage == 0) {
      for (Edge e : edges) {
        //check if it's an edge from the robot to any corner balls
        if (e.start.equals(vertices.get(0))
                && (e.end.ball != null &&
                (e.end.ball.getStrategy() == BallPickupStrategy.CORNER_BOTTOM_LEFT
                || e.end.ball.getStrategy() == BallPickupStrategy.CORNER_TOP_LEFT
                || e.end.ball.getStrategy() == BallPickupStrategy.CORNER_BOTTOM_RIGHT
                || e.end.ball.getStrategy() == BallPickupStrategy.CORNER_TOP_RIGHT))
        )
          e.cost = Integer.MAX_VALUE;
      }
    }

    //updates edges, so they only contain those needed in the shortestpath
    edges = findEdgesInShortestPath(vertices, edges);

    //update vertecies in order of visit
    vertices = listedByVisitingOrder(vertices, edges);

    vertices.forEach(v -> {
      if (v.ball != null)
        System.out.print(v.ball.getColor() + "\t");
      System.out.println(v.type);
    });

    System.out.println("");

    //check if multiple goal runs are needed
    if (maxAmountOfBallsInRobot < course.getBalls().size() + numberOfBallsInStorage){
      int amountOfBallsToCollectBeforeFirstGoal = course.getBalls().size() - 1 - maxAmountOfBallsInRobot;
      vertices = planExtraGoal(vertices, amountOfBallsToCollectBeforeFirstGoal);
    }
    vertices.add(new Vertex(goal,Type.GOAL));

    //finally update the actual plan
    plan = new ArrayDeque<>(vertices);

    plan.forEach(v -> {
      if (v.ball != null) System.out.print(v.ball.getColor());
      System.out.println(v.type);
    });

    //plan.pop();

  }

  private List<Vertex> planExtraGoal(List<Vertex> vertices, int amountBefore) {
    System.out.println("amount before: " + amountBefore);
    //check if orange ball exsist
    Ball orangeBall = course.getBalls().stream().filter(b -> b.getColor() == BallColor.ORANGE).findFirst().orElse(null);
    List<Vertex> newRoute = new ArrayList<>();

    //copy the elements before
    for(int i = 0; i < amountBefore; i++) newRoute.add(vertices.get(i + 1));

    //deal if orangeBall is on field and adds the goal too
    if (orangeBall != null)
      newRoute.add(new Vertex(orangeBall));

    //set up the new remaining verticies to check if more optimal route exist.
    List<Vertex> verteciesAfterGoal = new ArrayList<>();
    verteciesAfterGoal.add(new Vertex(goal, Type.GOAL));
    verteciesAfterGoal.add(new Vertex(goal, Type.GOAL));
    verteciesAfterGoal.addAll(vertices.subList(amountBefore + 1, vertices.size()));
    List<Edge> edgesAfterGoal = setupEdge(verteciesAfterGoal);
    edgesAfterGoal = findEdgesInShortestPath(verteciesAfterGoal, edgesAfterGoal);
    verteciesAfterGoal = listedByVisitingOrder(verteciesAfterGoal, edgesAfterGoal);

    newRoute.addAll(verteciesAfterGoal);
    return newRoute;
  }

  /**
   * pops the next element in the plan queue and makes the rc run it.
   *
   * @param rc need the routingcontroller to execute the next step
   * @throws IllegalStateException - if the list is empty, a recompute is needed first. Reason that this is not doing it, is because the task might be done.
   */
  @Override
  public void getComputedRoute(RoutingController rc) throws IllegalStateException{
    if (plan.isEmpty()){
      throw new IllegalStateException("Queue is empty, run recompute");
    }
    Vertex next = plan.pop();
    if (next.ball == null){
      rc.addRoutine(goal, true);
    } else
      rc.addRoutine(next.ball);
  }


  /**
   *
   *
   * @return - list of vertex in the graph
   */
  private List<Vertex> setupVertex() {
    List<Vertex> vertices = new ArrayList<>();
    //placing robot as the first element
    vertices.add(new Vertex(course.getRobot().getCenter(), Type.ROBOT));

    //placing the goal as the second element
    vertices.add(new Vertex(goal, Type.GOAL));

    //placing all balls
    List<Ball> balls = new ArrayList<>(course.getBalls());
    //if there is no balls, the robot should go to the goal
    if (balls.isEmpty()) {
      //TODO: write this shit!
      return vertices;
    }
    balls.stream()
            .filter(ball -> ball.getColor() == BallColor.WHITE)
            .forEach(ball -> {
      vertices.add(new Vertex(ball));
    });
    return vertices;
  }

  private List<Edge> setupEdge(final List<Vertex> vertices) {
    List<Edge> edges = new ArrayList<>();
    for (int i = 0; i < vertices.size(); i++) {
      for (int j = i + 1; j < vertices.size(); j++) {
        //never make directly from robot to goal
        if (i == 0 && j == 1) continue;
        //creates the edges
        edges.add(
                new Edge(
                        vertices.get(i), vertices.get(j),
                        PathCost(vertices.get(i).position, vertices.get(j).position)
                )
        );
      }
    }
    return edges;
  }

  /**
   * computes the expected time a path would take
   *
   * @param src where it start
   * @param dst where it ends
   * @return expected cost
   */
  private int PathCost(Point src, Point dst) {
    return (int) Geometry.distanceBetweenTwoPoints(src, dst);
    //TODO: actually compute this value
  }

  /**
   * find the edges needed in the shortest path
   *
   * @param vertices - all vertex
   * @param edges - must only points to objects from the passed vertex list.
   * @return a sublist of edges, that is needed in shortest path.
   */
  private @NotNull List<Edge> findEdgesInShortestPath(List<Vertex> vertices, List<Edge> edges) {
    if (edges.size() == vertices.size() - 1){
      return edges;
    }

    //Setup union find containing
    IUnionFind<Vertex> unionFind = new QuickFind<>();
    unionFind.init(vertices);

    //we make a fake connection between the robot and the goal
    //unionFind.union(vertices.get(0), vertices.get(1));

    //list of how many vertex each at max can have.
    //balls can have 2, robot and goal have 1
    List<Pair<Vertex, Integer>> remainingVertices = new ArrayList<>();
    remainingVertices.add(new Pair<>(vertices.get(0),1));
    remainingVertices.add(new Pair<>(vertices.get(1),1));
    for (int i = 2; i < vertices.size(); i++){
      remainingVertices.add(new Pair<>(vertices.get(i), 2));
    }

    //sorting my edges by travel cost, and pushing it onto a queue instead
    Collections.sort(edges);

    //need to have vertex equal to numbers of elements on board - 1
    int remainingElements = vertices.size() - 1;

    //saving the edges needed for shortest path.
    List<Edge> edgesInShortestPath = new ArrayList<>();

    //make a union between 0 and 1 to ensure we don't get a subcycle
    unionFind.union(vertices.get(0), vertices.get(1));

    for (Edge current: edges) {

      int position1 = vertices.indexOf(current.start);
      int position2 = vertices.indexOf(current.end);
      if (remainingVertices.get(position1).getSecond() == 0 || remainingVertices.get(position2).getSecond() == 0)
        continue;
      boolean success = unionFind.union(current.start, current.end);
      if (success) {
        edgesInShortestPath.add(current);
        remainingVertices.get(position1).setSecond(remainingVertices.get(position1).getSecond() - 1);
        remainingVertices.get(position2).setSecond(remainingVertices.get(position2).getSecond() - 1);
        remainingElements--;
      }
    }

    //find the last remaining edge
    boolean found = false;
    for(int i = 0; i < vertices.size(); i++){
      if (found) break;
      for (int j = i + 1; j < vertices.size(); j++){
        if (remainingVertices.get(i).getSecond() == 1 && remainingVertices.get(j).getSecond() == 1) {
          edgesInShortestPath.add(new Edge(vertices.get(i), vertices.get(j), 0));
          found = true;
          break;
        }
      }
    }

    return edgesInShortestPath;
  }

  /**
   * figures out what order based on the original position of the robot
   *
   * @param vertices - list of all vertecies
   * @param edges - list of edges between vertecies. each vertex must at max be reprecented in 2 edges.
   * @return a sorted list of vertex, based on visiting order.
   */
  private @NotNull List<Vertex> listedByVisitingOrder(List<Vertex> vertices, List<Edge> edges) {
    //the new list needed to return
    List<Vertex> updatedOrder = new ArrayList<>();

    //Make a copy, as we need to modify it but also use the original later.
    List<Edge> myEdges = new ArrayList<>(edges);

    //always start at robot
    Vertex from = vertices.get(0);
    updatedOrder.add(from);

    while (updatedOrder.size() != vertices.size() - 1) {
      Edge toRemove = null;
      for (Edge e : myEdges) {
        if (from.equals(e.start)) {
          toRemove = e;
          updatedOrder.add(e.end);
          from = e.end;
          break;
        } else if (from.equals(e.end)) {
          toRemove = e;
          updatedOrder.add(e.start);
          from = e.start;
          break;
        }
      }
      if (toRemove != null) myEdges.remove(toRemove);
    }

    return updatedOrder;
  }

  /**
   * used for data needed in each graph note
   */
  public class Vertex {
    Point position;
    Type type;
    Ball ball;

    Vertex(Point position, Type type) {
      this.position = position;
      this.type = type;
      ball = null;
    }

    Vertex(Ball ball) {
      this.position = ball.getCenter();
      this.type = Type.BALL;
      this.ball = ball;
    }
  }

  /**
   * This class is used to store all the data needed for the edges in the graph
   */
  private class Edge implements Comparable<Edge> {
    Vertex start, end;
    double cost;

    Edge(Vertex start, Vertex end, double cost) {
      this.start = start;
      this.end = end;
      this.cost = cost;
    }

    /**
     * A way to compare edges in order of path cost.
     *
     * @param o the object to be compared.
     * @return path cost
     */
    @Override
    public int compareTo(@NotNull HamiltonianRoute.Edge o) {
      return (int) (this.cost - o.cost);
    }
  }

  enum Type {
    ROBOT, BALL, GOAL
  }
}
