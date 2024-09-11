package starengine;

import starengine.units.Units;
import starengine.units.UnitsFromFakes;

public class StarEngine {
    public Map map;
    public Units units;

    public StarEngine() {
        Images.loadAllImages();

        map = new Map();
        map.init();

//        units = new SimpleUnits(this, map);
        units = new UnitsFromFakes(this, map);
    }

    public void updateOnFrameEnd() {
        EngineUpdater.update(this);
    }
}
