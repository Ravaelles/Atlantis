package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.buildings.TerranDontEngageWhenCombatBuildings;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.protoss.ProtossDragoonCombatManager;
import atlantis.protoss.ProtossZealotCombatManager;
import atlantis.protoss.ht.ProtossHTCombatManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class TerranCombatManager extends MobileDetector {
    public TerranCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranDontEngageWhenCombatBuildings.class,
        };
    }
}
