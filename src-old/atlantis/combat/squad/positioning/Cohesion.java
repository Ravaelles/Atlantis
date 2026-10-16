package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
//            ProtossCohesion.class,
//            TerranCohesion.class,
        };
    }
}
