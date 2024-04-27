package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.units.AUnit;
import atlantis.units.workers.WorkerAvoidManager;

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
            AvoidEnemiesIfNeeded.class,
            ScoutAvoidCombatBuildings.class,
            ScoutSafetyFarFromEnemy.class,
            ScoutFreeBases.class,
            ScoutRoaming.class,
            WorkerAvoidManager.class,
            ScoutTryFindingEnemy.class,
            RoamAroundEnemyBase.class,
//            TestRoamingAroundBase.class,
        };
    }

    @Override
    protected Manager handle() {
        unit.setTooltipTactical("Scout...");

//        CameraCommander.centerCameraOn(unit);

        if (unit.isRepairing()) return usedManager(this, "UhmRepairing");

        return handleSubmanagers();
    }
}
