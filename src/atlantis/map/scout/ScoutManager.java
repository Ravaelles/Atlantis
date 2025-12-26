package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.avoid.special.protoss.ProtossAvoidCriticalUnits;
import atlantis.map.scout.enemy.ScoutNearEnemyBase;
import atlantis.units.AUnit;

public class ScoutManager extends Manager {
    public ScoutManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossAvoidEnemies.class,
            ScoutSeparateFromCloseEnemies.class,
            ScoutSeparateFromCloseWorkers.class,
//            ScoutSafetyAvoidTooCloseEnemies.class,
            ProtossAvoidCriticalUnits.class,
            ScoutAvoidCombatBuildings.class,

            ScoutEnemyNaturalIfNotExisting.class,
            ScoutEnemyThird.class,

            ScoutTryFindingEnemy.class,

            ScoutPotentialTerranBases.class,
            ScoutNearEnemyBase.class,

            ScoutUnexploredBasesNearEnemy.class,
            ScoutPotentialEnemyBases.class,

            ScoutFreeBases.class,

//            ScoutRoaming.class,
//            WorkerAvoidManager.class,
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
