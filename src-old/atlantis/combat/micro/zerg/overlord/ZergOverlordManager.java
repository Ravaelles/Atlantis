package atlantis.combat.micro.zerg.overlord;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.stack.StackedUnitsManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ZergOverlordManager extends Manager {
    public ZergOverlordManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Zerg_Overlord);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossAvoidEnemies::new,
            StackedUnitsManager::new,
            WeDontKnowEnemyLocation::new,
            WeKnowEnemyLocation::new,
        };
    }

}
