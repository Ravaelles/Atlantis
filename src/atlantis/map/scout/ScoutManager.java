package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.map.scout.enemy.ScoutNearEnemyBase;
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
            ScoutSeparateFromCloseEnemies.class,
//            ScoutSafetyAvoidTooCloseEnemies.class,
            AvoidEnemies.class,
            ScoutAvoidCombatBuildings.class,

            ScoutTryFindingEnemy.class,

            ScoutNearEnemyBase.class,

            ScoutEnemyThird.class,
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
