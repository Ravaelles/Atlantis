package jbweb;

import bwapi.*;
import bwem.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathCache {
    HashMap<Pair<TilePosition, TilePosition>, List<Path>> indexList = new HashMap<>();
    int pathCacheIndex = 0;
    List<Path> pathCache = new ArrayList<>();
    HashMap<Area, Integer> notReachableThisFrame = new HashMap<>();
}
