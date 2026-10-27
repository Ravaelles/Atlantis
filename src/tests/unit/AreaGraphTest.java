package tests.unit;

import atlantis.map.analysis.AreaGraph;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M3 model slice: ground connectivity and drop candidates over a hand-built
 * area graph - no BWEM, no game. A line of three areas plus one island pins
 * BFS reachability, backdoor ranking and the worthless-area exclusion.
 */
@Tag("map")
public class AreaGraphTest {

    private static AreaGraph graph() {
        List<AreaGraph.AreaNode> nodes = new ArrayList<>();
        nodes.add(new AreaGraph.AreaNode(1, APosition.create(10, 10), true, 8));
        nodes.add(new AreaGraph.AreaNode(2, APosition.create(30, 10), false, 8));
        nodes.add(new AreaGraph.AreaNode(3, APosition.create(50, 10), true, 8));
        nodes.add(new AreaGraph.AreaNode(9, APosition.create(10, 100), true, 8));

        List<AreaGraph.ChokeEdge> edges = new ArrayList<>();
        edges.add(new AreaGraph.ChokeEdge(1, 2, 64));
        edges.add(new AreaGraph.ChokeEdge(2, 3, 64));

        return AreaGraph.of(nodes, edges);
    }

    @Test
    public void groundPathsFollowChokeLinks() {
        AreaGraph graph = graph();

        assertTrue(graph.groundConnected(1, 3));
        assertTrue(graph.groundConnected(3, 1));
        assertTrue(graph.groundConnected(2, 2));
        assertFalse(graph.groundConnected(1, 9));
        assertFalse(graph.groundConnected(9, 2));
        assertFalse(graph.groundConnected(1, 42));
    }

    @Test
    public void onlyDisconnectedEnemyAreasAreDropCandidates() {
        AreaGraph graph = graph();
        Set<Integer> enemy = new HashSet<>();
        enemy.add(3);
        enemy.add(9);

        List<AreaGraph.DropCandidate> candidates = graph.dropCandidates(1, enemy);

        assertEquals(1, candidates.size());
        assertEquals(9, candidates.get(0).areaId());
    }

    @Test
    public void candidatesRankByFlightDistance() {
        List<AreaGraph.AreaNode> nodes = new ArrayList<>();
        nodes.add(new AreaGraph.AreaNode(1, APosition.create(10, 10), true, 8));
        nodes.add(new AreaGraph.AreaNode(8, APosition.create(100, 100), true, 8));
        nodes.add(new AreaGraph.AreaNode(9, APosition.create(20, 10), true, 8));
        AreaGraph graph = AreaGraph.of(nodes, new ArrayList<AreaGraph.ChokeEdge>());

        Set<Integer> enemy = new HashSet<>();
        enemy.add(8);
        enemy.add(9);

        List<AreaGraph.DropCandidate> candidates = graph.dropCandidates(1, enemy);

        assertEquals(2, candidates.size());
        assertEquals(9, candidates.get(0).areaId());
        assertEquals(8, candidates.get(1).areaId());
    }

    @Test
    public void worthlessAreasAreNoDrop() {
        List<AreaGraph.AreaNode> nodes = new ArrayList<>();
        nodes.add(new AreaGraph.AreaNode(1, APosition.create(10, 10), true, 8));
        nodes.add(new AreaGraph.AreaNode(9, APosition.create(10, 100), false, 0));
        AreaGraph graph = AreaGraph.of(nodes, new ArrayList<AreaGraph.ChokeEdge>());

        Set<Integer> enemy = new HashSet<>();
        enemy.add(9);

        assertEquals(0, graph.dropCandidates(1, enemy).size());
    }
}
