package atlantis.combat.micro.generic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.generic.managers.DetectHiddenEnemyClosestToBase;
import atlantis.combat.micro.generic.managers.FollowArmy;
import atlantis.combat.micro.generic.managers.SpreadOutDetectors;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.terran.repair.managers.GoToRepairAsAirUnit;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidEnemies.class,
            UnitBeingReparedManager.class,
            DetectHiddenEnemyClosestToBase.class,
            FollowArmy.class,
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
