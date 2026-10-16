package atlantis.combat.managers.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.buildings.TerranDontEngageWhenCombatBuildings;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.terran.marine.TerranMarine;
import atlantis.units.AUnit;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranMarine::new,
            TerranDontEngageWhenCombatBuildings::new,
        };
    }
}
