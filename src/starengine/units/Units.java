package starengine.units;

import starengine.Map;
import starengine.StarEngine;

import java.awt.*;
import java.util.List;

import static starengine.Map.SPACE_HEIGHT;
import static starengine.Map.SPACE_WIDTH;

public abstract class Units {
    public static final int UNIT_WIDTH = 36;
    public static final int UNIT_HEIGHT = 48;

    protected StarEngine engine;
    protected Map map;

    public Units(StarEngine engine, Map map) {
        this.engine = engine;
        this.map = map;
    }

//    public abstract void updateUnits();

    public void updateUnits() {
        for (Unit unit : allUnits()) {
            unit.move();

            // Check if the unit is inside a non-walkable area
            for (Rectangle nonWalkableArea : engine.map.nonWalkableAreas) {
                if (nonWalkableArea.contains(unit.x(), unit.y())) {
                    unit.reverseDirection();
                    break;
                }
            }

            // Check if the unit is within the starEngine bounds
            if (unit.x() < UNIT_WIDTH || unit.x() > SPACE_WIDTH - UNIT_WIDTH) {
                unit.reverseXDirection();
            }
            if (unit.y() < UNIT_WIDTH || unit.y() > SPACE_HEIGHT - UNIT_WIDTH) {
                unit.reverseYDirection();
            }
        }
    }

    public abstract List<Unit> allUnits();

}
