package starengine.units;

import starengine.assets.Map;
import starengine.StarEngine;

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
//        for (FakeUnit unit : allUnits()) {
//            unit.move();
//
//            // Check if the unit is inside a non-walkable area
//            for (Rectangle nonWalkableArea : engine.map.nonWalkableAreas) {
//                if (nonWalkableArea.contains(unit.x(), unit.y())) {
//                    unit.reverseDirection();
//                    break;
//                }
//            }
//
//            // Check if the unit is within the starEngine bounds
//            if (unit.x() < UNIT_WIDTH || unit.x() > SPACE_WIDTH - UNIT_WIDTH) {
//                unit.reverseXDirection();
//            }
//            if (unit.y() < UNIT_WIDTH || unit.y() > SPACE_HEIGHT - UNIT_WIDTH) {
//                unit.reverseYDirection();
//            }
//        }
    }

//    public abstract List<FakeUnit> allUnits();

}
