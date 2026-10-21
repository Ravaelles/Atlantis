package atlantis.battle.detectors;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.battle.avoid.protoss.ProtossAvoidEnemies;
import atlantis.battle.cluster.Alpha;
import atlantis.terran.repair.managers.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MobileDetector extends Manager {
    protected AUnit unitAssignedToMainSquad = null;
    protected AUnit unitForSquadScout = null;
    protected AUnit unitForBase = null;

    // =========================================================

    public MobileDetector(AUnit unit) {
        super(unit);
    }

    // =========================================================

    @Override
    public boolean applies() {
        return unit.type().isDetectorNonBuilding();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossAvoidEnemies::new,
            UnitBeingReparedManager::new,
            SpreadOutDetectors::new,
            DetectHiddenEnemyClosestToBase::new,
            FollowArmy::new,
        };
    }

//    private boolean update() {
////        if (handleSpreadOut()) return true;
////        if (detectInvisibleUnitsClosestToBase()) return true;
////        if (followArmy(false)) return true;
////        if (followArmy(true)) return true;
//        if (followSquadScout()) return true;
//        return false;
//
////        return followArmy(true);
//    }

    // =========================================================

    protected boolean followSquadScout() {
        if (!unit.is(unitForSquadScout)) return false;

        AUnit scout = Alpha.get().squadScout();
        if (scout != null) {
            unitForSquadScout = unit;
            if (scout.distTo(unit) > 1) {
                unitForSquadScout.move(scout, Actions.MOVE_FOLLOW, "FollowScout", true);
            }
            return true;
        }

        return false;
    }
}
