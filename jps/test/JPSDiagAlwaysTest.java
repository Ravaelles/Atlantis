package jps.test;

import jps.main.java.jps.Graph;
import jps.main.java.jps.Tile;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Kevin
 */
public class JPSDiagAlwaysTest extends JPSDiagBaseTest {
    @Before
    public void setup() {
        setup(Graph.Diagonal.ALWAYS);
    }

    @Test
    public void twoObstaclesAndPath() throws ExecutionException, InterruptedException {
        Tile start = tileList.get(4).get(0);
        Tile end = tileList.get(0).get(8);
        for (int i = 1; i < 5; i++) {
            for (int j = 5; j < 9; j++) {
                tileList.get(i).get(j).walkable = false;
            }
        }
        for (int i = 0; i < 4; i++) {
            tileList.get(2).get(i).walkable = false;
        }
        tileList.get(0).get(3).walkable = false;
        tileList.get(1).get(4).walkable = false;

        Future<Queue<Tile>> futurePath = jps.findPath(start, end);
        Queue<Tile> path = futurePath.get();

        assert path != null;
        assert !path.isEmpty();

        assert path.remove().equals(tileList.get(4).get(0));
        assert path.remove().equals(tileList.get(3).get(1));
        assert path.remove().equals(tileList.get(3).get(2));
        assert path.remove().equals(tileList.get(3).get(3));
        assert path.remove().equals(tileList.get(2).get(4));
        assert path.remove().equals(tileList.get(1).get(3));
        assert path.remove().equals(tileList.get(0).get(4));
        assert path.remove().equals(tileList.get(0).get(5));
        assert path.remove().equals(tileList.get(0).get(6));
        assert path.remove().equals(tileList.get(0).get(7));
        assert path.remove().equals(tileList.get(0).get(8));
    }

    @Test
    public void comingFromTopLeftThroughTwoObstacles() throws ExecutionException, InterruptedException {
        Tile start = tileList.get(0).get(0);
        Tile end = tileList.get(4).get(4);
        for (int i = 0; i < 4; i++) {
            tileList.get(2).get(i).walkable = false;
        }
        tileList.get(1).get(4).walkable = false;

        Future<Queue<Tile>> futurePath = jps.findPath(start, end);
        Queue<Tile> path = futurePath.get();

        assert path != null;
        assert !path.isEmpty();

        assert path.remove().equals(tileList.get(0).get(0));
        assert path.remove().equals(tileList.get(1).get(1));
        assert path.remove().equals(tileList.get(1).get(2));
        assert path.remove().equals(tileList.get(1).get(3));
        assert path.remove().equals(tileList.get(2).get(4));
        assert path.remove().equals(tileList.get(3).get(4));
        assert path.remove().equals(tileList.get(4).get(4));
    }

    @Test
    public void noPath() throws ExecutionException, InterruptedException {
        Tile start = tileList.get(0).get(0);
        Tile end = tileList.get(4).get(8);

        for (Tile tile : tileList.get(2)) {
            tile.walkable = false;
        }

        Future<Queue<Tile>> futurePath = jps.findPath(start, end);
        Queue<Tile> path = futurePath.get();

        assert path == null;
    }

    @Test
    public void map2() throws ExecutionException, InterruptedException {
        Tile start = tileList2.get(38).get(34);
        Tile end = tileList2.get(38).get(33);

        //Future<Queue<Tile>> futurePath = jps2.findPath(start, end);
        Queue<Tile> path = jps2.findPathSync(start, end);

        for (Tile tile : path) {
//            System.out .pr intln("X: " + tile.x + ", Y: " + tile.y);
        }

        assert path != null;
    }
}
