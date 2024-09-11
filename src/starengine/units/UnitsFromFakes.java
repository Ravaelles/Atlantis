package starengine.units;

import starengine.Map;
import starengine.StarEngine;

public class UnitsFromFakes extends Units {
    protected StarEngine engine;
    protected Map map;

    public UnitsFromFakes(StarEngine engine, Map map) {
        super(engine, map);
    }

//    public List<FakeUnit> allUnits() {
//        return FakeUnitToEngineUnits.convert(Select.all().list());
//    }
//    public List<FakeUnit> allUnits() {
//        return ;
//    }
//
//    public List<FakeUnit> ourUnits() {
//        return FakeUnitToEngineUnits.convert(Select.all().our.list());
//    }
}
