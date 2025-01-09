package atlantis.combat.managers;

import atlantis.protoss.dragoon.ProtossDragoonLongNotAttacked;
import atlantis.protoss.zealot.ProtossZealotLongNotAttacked;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossTopCombatManager extends Manager {
    public ProtossTopCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss() && unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossDragoonLongNotAttacked.class,
            ProtossZealotLongNotAttacked.class,
        };
    }
}
