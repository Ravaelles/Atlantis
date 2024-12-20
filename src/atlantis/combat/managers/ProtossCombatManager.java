package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.protoss.dragoon.ProtossDragoonCombatManager;
import atlantis.protoss.ProtossZealotCombatManager;
import atlantis.protoss.ht.ProtossHTCombatManager;
import atlantis.protoss.reaver.ProtossReaver;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossCombatManager extends MobileDetector {
    public ProtossCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            //            ProtossDontEngageWhenCombatBuildings.class,
//            ProtossTooLonelyGetCloser.class,

            ProtossDragoonCombatManager.class,
            ProtossZealotCombatManager.class,
            ProtossHTCombatManager.class,
            ProtossReaver.class,
        };
    }
}
