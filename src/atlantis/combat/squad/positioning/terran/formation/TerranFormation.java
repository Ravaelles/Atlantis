package atlantis.combat.squad.positioning.terran.formation;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.terran.formation.moon.TerranMoon;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranFormation extends Manager {
    public TerranFormation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranMoon.class,
        };
    }
}
