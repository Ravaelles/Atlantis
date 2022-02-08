package jps;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Kevin
 */
public abstract class JPS<T extends Node> {
    protected final Graph<T> graph;

    public JPS(Graph<T> graph) {
        this.graph = graph;
    }

    public Future<Queue<T>> findPath(T start, T goal) {
        return findPath(start, goal, false, false);
    }

    public Future<Queue<T>> findPath(T start, T goal, boolean adjacentStop) {
        return findPath(start, goal, adjacentStop, true);
    }

    public Future<Queue<T>> findPath(T start, T goal, boolean adjacentStop, boolean diagonalStop) {
        FutureTask<Queue<T>> future = new FutureTask<>(() -> findPathSync(start, goal, adjacentStop, diagonalStop));
        future.run();
        return future;
    }

    public Queue<T> findPathSync(T start, T goal) {
        return findPathSync(start, goal, false, false);
    }

    public Queue<T> findPathSync(T start, T goal, boolean adjacentStop) {
        return findPathSync(start, goal, adjacentStop, true);
    }

    public Queue<T> findPathSync(T start, T goal, boolean adjacentStop, boolean diagonalStop) {
        Map<T, Double> fMap = new HashMap<>(); // distance to start + estimate to end
        Map<T, Double> gMap = new HashMap<>(); // distance to start (parent's g + distance from parent)
        Map<T, Double> hMap = new HashMap<>(); // estimate to end

        Queue<T> open = new PriorityQueue<>((a, b) -> {
            // we want the nodes with the lowest projected F value to be checked first
            return Double.compare(fMap.getOrDefault(a, 0d), fMap.getOrDefault(b, 0d));
        });
        Set<T> closed = new HashSet<>();
        Map<T, T> parentMap = new HashMap<>();
        Set<T> goals = new HashSet<>();

        if (adjacentStop) {
            if (!diagonalStop)
                goals = graph.getNeighborsOf(goal, Graph.Diagonal.NEVER);
            else
                goals = findNeighbors(goal, parentMap);
        }
        if (goal.isWalkable()) {
            goals.add(goal);
        }
        if (goals.isEmpty()) {
            return null;
        }

        System.out.println("Start: " + start.x + "," + start.y);
        // push the start node into the open list
        open.add(start);

        // while the open list is not empty
        while (!open.isEmpty()) {
            //System.out.println(open.size());
            // pop the position of node which has the minimum `f` value.
            T node = open.poll();
            // mark the current node as checked
            closed.add(node);

            if (goals.contains(node)) {
                return backtrace(node, parentMap);
            }
            // add all possible next steps from the current node
            identifySuccessors(node, goal, goals, open, closed, parentMap, fMap, gMap, hMap);
        }

        // failed to find a path
        return null;
    }

    /**
     * Identify successors for the given node. Runs a JPS in direction of each available neighbor, adding any open
     * nodes found to the open list.
     * @return All the nodes we have found jumpable from the current node
     */
    private void identifySuccessors(T node, T goal, Set<T> goals, Queue<T> open, Set<T> closed, Map<T, T> parentMap,
                                    Map<T, Double> fMap, Map<T, Double> gMap, Map<T, Double> hMap) {
        // get all neighbors to the current node
        Collection<T> neighbors = findNeighbors(node, parentMap);

        double d;
        double ng;
        for (T neighbor : neighbors) {
            // jump in the direction of our neighbor
            T jumpNode = jump(neighbor, node, goals);

            // don't add a node we have already gotten to quicker
            if (jumpNode == null || closed.contains(jumpNode)) continue;

            // determine the jumpNode's distance from the start along the current path
            d = graph.getDistance(jumpNode, node);
            ng = gMap.getOrDefault(node, 0d) + d;

            // if the node has already been opened and this is a shorter path, update it
            // if it hasn't been opened, mark as open and update it
            if (!open.contains(jumpNode) || ng < gMap.getOrDefault(jumpNode, 0d)) {
                gMap.put(jumpNode, ng);
                hMap.put(jumpNode, graph.getHeuristicDistance(jumpNode, goal));
                fMap.put(jumpNode, gMap.getOrDefault(jumpNode, 0d) + hMap.getOrDefault(jumpNode, 0d));
                //System.out.println("jumpNode: " + jumpNode.x + "," + jumpNode.y + " f: " + fMap.get(jumpNode));
                parentMap.put(jumpNode, node);

                if (!open.contains(jumpNode)) {
                    open.offer(jumpNode);
                }
            }
        }
    }

    /**
     * Find all neighbors for a given node. If node has a parent then prune neighbors based on JPS algorithm,
     * otherwise return all neighbors.
     */
    protected abstract Set<T> findNeighbors(T node, Map<T, T> parentMap);

    /**
     * Search towards the child from the parent, returning when a jump point is found.
     */
    protected abstract T jump(T neighbor, T current, Set<T> goals);

    /**
     * Returns a path of the parent nodes from a given node.
     */
    private Queue<T> backtrace(T node, Map<T, T> parentMap) {
        LinkedList<T> path = new LinkedList<>();
        path.add(node);

        int previousX, previousY, currentX, currentY;
        int dx, dy;
        int steps;
        T temp;
        while (parentMap.containsKey(node)) {
            previousX = parentMap.get(node).x;
            previousY = parentMap.get(node).y;
            currentX = node.x;
            currentY = node.y;
            steps = Integer.max(Math.abs(previousX - currentX), Math.abs(previousY - currentY));
            dx = Integer.compare(previousX, currentX);
            dy = Integer.compare(previousY, currentY);

            temp = node;
            for (int i = 0; i < steps; i++) {
                temp = graph.getNode(temp.x + dx, temp.y + dy);
                path.addFirst(temp);
            }

            node = parentMap.get(node);
        }
        return path;
    }

    public static class JPSFactory {
        public static <T extends Node> JPS<T> getJPS(Graph<T> graph, Graph.Diagonal diagonal) {
            switch (diagonal) {
                case ALWAYS:
                    return new JPSDiagAlways<>(graph);
                case ONE_OBSTACLE:
                    return new JPSDiagOneObstacle<>(graph);
                case NO_OBSTACLES:
                    return new JPSDiagNoObstacles<>(graph);
                case NEVER:
                    return new JPSDiagNever<>(graph);
            }
            return null;
        }
    }
}
