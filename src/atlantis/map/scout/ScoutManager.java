package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;

public class ScoutManager extends Manager {
    public ScoutManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScout();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidEnemies.class,
            ScoutRunning.class,
            TryFindingEnemy.class,
            RoamAroundEnemyBase.class,
            ScoutFreeBases.class,
//            TestRoamingAroundBase.class,
        };
    }

    @Override
    protected Manager handle() {
        unit.setTooltipTactical("Scout...");

        if (unit.isRepairing()) return usedManager(this, "UhmRepairing");

        return handleSubmanagers();
    }
}
