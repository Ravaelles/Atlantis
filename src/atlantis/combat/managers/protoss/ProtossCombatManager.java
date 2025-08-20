package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.protoss.dragoon.DragoonCombatManager;
import atlantis.protoss.dt.DarkTemplar;
import atlantis.protoss.reaver.Reaver;
import atlantis.protoss.zealot.ProtossZealotCombatManager;
import atlantis.protoss.ht.ProtossHTCombatManager;
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

            DragoonCombatManager.class,
            ProtossZealotCombatManager.class,
            ProtossHTCombatManager.class,
            Reaver.class,
        };
    }
}
