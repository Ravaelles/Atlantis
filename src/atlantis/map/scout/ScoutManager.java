package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossAvoidEnemies::new,
            ScoutSeparateFromCloseEnemies::new,
            ScoutSeparateFromCloseWorkers::new,
//            ScoutSafetyAvoidTooCloseEnemies.class,
            ProtossAvoidCriticalUnits::new,
            ScoutAvoidCombatBuildings::new,

            ScoutEnemyNaturalIfNotExisting::new,
            ScoutEnemyThird::new,

            ScoutTryFindingEnemy::new,

            ScoutPotentialTerranBases::new,
            ScoutNearEnemyBase::new,

            ScoutUnexploredBasesNearEnemy::new,
            ScoutPotentialEnemyBases::new,

            ScoutFreeBases::new,

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
