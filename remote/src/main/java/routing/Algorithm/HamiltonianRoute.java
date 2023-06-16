package routing.Algorithm;

import courseObjects.Ball;
import courseObjects.Border;
import courseObjects.Course;
import math.Geometry;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import routing.Algorithm.UnionFind.IUnionFind;
import routing.Algorithm.UnionFind.QuickFind;
import routing.RoutingController;
import vision.BallPickupStrategy;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HamiltonianRoute implements IRoutePlanner {
  //TODO: get these from the right place...
  Point goal;
  Deque<Vertex> plan; //TODO: lav denne til en queue i stedet
  Course course;

  @Override
  public void computeFullRoute(Course course, int numberOfBallsInStorage) {
    this.goal = course.getBorder().getSmallGoalMiddlePoint();

    //initialization, but not pushing to the actual one, as it might be used while recomputing route
    //List<Routine> planning = new ArrayList<>();


    //saving the course in class for now, until i know how many private methods need it aswell
    //TODO: remove?
    this.course = course;

    List<Vertex> vertices = new ArrayList<>();
    setupVertex(vertices);

    //if there is 1 or fewer vertices, there can't be made any path
    if (vertices.size() <= 1) return;

    List<Edge> edges = new ArrayList<>();
    setupEdge(edges, vertices);

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

    //finally update the actual plan
    plan = new ArrayDeque<>(vertices);


  }

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

  private void setupVertex(final List<Vertex> vertices) {
    //placing robot as the first element
    vertices.add(new Vertex(course.getRobot().getCenter(), Type.ROBOT));

    //placing the goal as the second element
    vertices.add(new Vertex(goal, Type.GOAL));

    //placing all balls
    List<Ball> balls = course.getBalls();
    //if there is no balls, the robot should go to the goal
    if (balls.isEmpty()) {
      //TODO: write this shit!
      return;
    }
    balls.forEach(ball -> {
      vertices.add(new Vertex(ball.getCenter(), ball));
    });
  }

  private void setupEdge(final List<Edge> edges, final List<Vertex> vertices) {
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
  }

  private int PathCost(Point src, Point dst) {
    return (int) Geometry.distanceBetweenTwoPoints(src, dst);
    //TODO: actually compute this value
  }

  private List<Edge> findEdgesInShortestPath(List<Vertex> vertices, List<Edge> edges) {
    //Setup union find containing
    IUnionFind<Vertex> unionFind = new QuickFind<>();
    unionFind.init(vertices);

    //we make a fake connection between the robot and the goal
    unionFind.union(vertices.get(0), vertices.get(1));

    //list of how many vertex each at max can have.
    //balls can have 2, robot and goal have 1
    int[] remainingVertices = new int[vertices.size()];
    Arrays.fill(remainingVertices, 2);
    remainingVertices[0] = 1;
    remainingVertices[1] = 1;

    //sorting my edges by travel cost, and pushing it onto a queue instead
    Collections.sort(edges);
    Deque<Edge> queueOfEdges = new ArrayDeque<>(edges);

    //need to have vertex equal to numbers of elements on board - 1
    int remainingElements = vertices.size() - 1;

    //saving the edges needed for shortest path.
    List<Edge> edgesInShortestPath = new ArrayList<>();

    while (remainingElements != 0) {
      //fail safe
      if (queueOfEdges.isEmpty()) break; //TODO: throw error

      Edge current = queueOfEdges.pop();
      boolean success = unionFind.union(current.start, current.end);
      if (success) {
        edgesInShortestPath.add(current);
        remainingElements--;
        int position = vertices.indexOf(current.start);
        if (--remainingVertices[position] == 0)
          queueOfEdges.removeIf(e ->
                  e.start.equals(current.start) || e.end.equals(current.start));
        position = vertices.indexOf(current.end);
        if (--remainingVertices[position] == 0)
          queueOfEdges.removeIf(e ->
                  e.start.equals(current.end) || e.end.equals(current.end));
      }
    }

    return edgesInShortestPath;
  }

  private List<Vertex> listedByVisitingOrder(List<Vertex> vertices, List<Edge> edges) {
    //the new list needed to return
    List<Vertex> updatedOrder = new ArrayList<>();

    //Make a copy, as we need to modify it but also use the original later. //TODO: maybe??
    List<Edge> myEdges = new ArrayList<>(edges);

    //always start at robot
    Vertex from = vertices.get(0);
    updatedOrder.add(from);

    while (updatedOrder.size() != vertices.size()) {
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

  //used for data needed in each graph note
  public class Vertex {
    Point position;
    Type type;
    Ball ball;

    Vertex(Point position, Type type) {
      this.position = position;
      this.type = type;
      ball = null;
    }

    Vertex(Point position, Ball ball) {
      this.position = position;
      this.type = Type.BALL;
      this.ball = ball;
    }
  }

  private class Edge implements Comparable<Edge> {
    Vertex start, end;
    double cost;

    Edge(Vertex start, Vertex end, double cost) {
      this.start = start;
      this.end = end;
      this.cost = cost;
    }

    @Override
    public int compareTo(@NotNull HamiltonianRoute.Edge o) {
      return (int) (this.cost - o.cost);
    }
  }

  enum Type {
    ROBOT, BALL, GOAL
  }
}
