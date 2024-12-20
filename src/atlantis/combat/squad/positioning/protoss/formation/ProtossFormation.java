package atlantis.combat.squad.positioning.protoss.formation;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.formation.crescent.ProtossCrescent;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossFormation extends Manager {
    public ProtossFormation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss() && unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossCrescent.class,
        };
    }
}
