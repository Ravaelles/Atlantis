package starengine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Map {
    public static final int SPACE_WIDTH = 1000;
    public static final int SPACE_HEIGHT = 1000;
    public List<Rectangle> nonWalkableAreas = new ArrayList<>();


    public void init() {
        initNonWalkable();
    }

    private void initNonWalkable() {
        // Initialize non-walkable areas (walls)
        nonWalkableAreas.add(new Rectangle(200, 200, 200, 400));
        nonWalkableAreas.add(new Rectangle(600, 200, 200, 400));
        nonWalkableAreas.add(new Rectangle(400, 200, 200, 50));
        nonWalkableAreas.add(new Rectangle(400, 550, 200, 50));
    }
}
