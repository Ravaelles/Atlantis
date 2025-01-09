package starengine.units;

import starengine.assets.Map;
import starengine.StarEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static starengine.assets.Map.SPACE_HEIGHT;
import static starengine.assets.Map.SPACE_WIDTH;

public class SimpleUnits extends Units {
    public static final int NUM_UNITS = 30;
    private List<Unit> allUnits;

    public SimpleUnits(StarEngine engine, Map map) {
        super(engine, map);
        init();
    }

    public void init() {
        this.allUnits = new ArrayList<>();

        // Initialize units
        for (int i = 0; i < NUM_UNITS; i++) {
            int x = new Random().nextInt(SPACE_WIDTH - 2 * Units.UNIT_WIDTH) + Units.UNIT_WIDTH;
            int y = new Random().nextInt(SPACE_HEIGHT - 2 * Units.UNIT_WIDTH) + Units.UNIT_WIDTH;
            int dx = new Random().nextInt(5) - 2; // Random speed in x direction (-2 to 2)
            int dy = new Random().nextInt(5) - 2; // Random speed in y direction (-2 to 2)

            allUnits.add(new Unit(x, y, dx, dy, Owner.NEUTRAL));
        }
    }

//    public void updateUnits() {
//        for (Unit unit : allUnits) {
//            unit.move();
//
//            // Check if the unit is inside a non-walkable area
//            for (Rectangle nonWalkableArea : engine.map.nonWalkableAreas) {
//                if (nonWalkableArea.contains(unit.getX(), unit.getY())) {
//                    unit.reverseDirection();
//                    break;
//                }
//            }
//
//            // Check if the unit is within the starEngine bounds
//            if (unit.getX() < UNIT_RADIUS || unit.getX() > SPACE_WIDTH - UNIT_RADIUS) {
//                unit.reverseXDirection();
//            }
//            if (unit.getY() < UNIT_RADIUS || unit.getY() > SPACE_HEIGHT - UNIT_RADIUS) {
//                unit.reverseYDirection();
//            }
//        }
//    }

    public List<Unit> allUnits() {
        return allUnits;
    }
}
