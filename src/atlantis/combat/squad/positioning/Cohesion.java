package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.ProtossCohesion;
import atlantis.combat.squad.positioning.terran.TerranCohesion;
import atlantis.units.AUnit;

public class Cohesion extends Manager {
    public Cohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            ProtossCohesion.class,
            TerranCohesion.class,
        };
    }
}
