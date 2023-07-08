package atlantis.combat.micro.zerg.overlord;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.stack.StackedUnitsManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.architecture.Manager;

public class ZergOverlordManager extends Manager {

    public ZergOverlordManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Zerg_Overlord);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            AvoidEnemies.class,
            StackedUnitsManager.class,
            WeDontKnowEnemyLocation.class,
            WeKnowEnemyLocation.class,
        };
    }

}
