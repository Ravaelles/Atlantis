package jps.test;

import jps.main.java.jps.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kevin
 */
public class JPSTestUtil {
    public static List<List<Tile>> arraysToLists(Tile[][] tiles) {
        List<List<Tile>> tileList = new ArrayList<>();

        for (Tile[] tileRow : tiles) {
            tileList.add(new ArrayList<>(Arrays.asList(tileRow)));
        }

        return tileList;
    }
}
