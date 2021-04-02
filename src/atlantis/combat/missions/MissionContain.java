package atlantis.combat.missions;

import atlantis.position.APosition;
import atlantis.units.AUnit;

public class MissionContain extends MissionAttack {

    protected MissionContain(String name) {
        super(name);
    }

    @Override
    public APosition focusPoint() {
        return getFocusPointManager().focusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        return getUnitManager().updateUnit(unit);
    }

}