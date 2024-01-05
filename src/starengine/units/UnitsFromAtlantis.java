package starengine.units;

import atlantis.units.select.Select;
import starengine.Map;
import starengine.StarEngine;
import tests.unit.FakeUnit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static starengine.Map.SPACE_HEIGHT;
import static starengine.Map.SPACE_WIDTH;

public class UnitsFromAtlantis extends Units {
    protected StarEngine engine;
    protected Map map;

    public UnitsFromAtlantis(StarEngine engine, Map map) {
        super(engine, map);
    }

    public List<Unit> allUnits() {
        return FakeUnitToEngineUnits.convert(Select.all().list());
    }

    public List<Unit> ourUnits() {
        return FakeUnitToEngineUnits.convert(Select.all().our.list());
    }
}
