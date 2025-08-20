package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.map.scout.enemy.ScoutNearEnemyBase;
import atlantis.units.AUnit;
import atlantis.util.CenterCamera;

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
            AvoidEnemies.class,
            ScoutSeparateFromCloseEnemies.class,
            ScoutSeparateFromCloseWorkers.class,
//            ScoutSafetyAvoidTooCloseEnemies.class,
            AvoidCriticalUnits.class,
            ScoutAvoidCombatBuildings.class,

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
