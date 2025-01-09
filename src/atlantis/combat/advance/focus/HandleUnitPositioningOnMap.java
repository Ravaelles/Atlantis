package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.advance.leader.AdvanceAsAlphaLeader;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.Cohesion;
import atlantis.units.AUnit;

public class HandleUnitPositioningOnMap extends MissionManager {
    public HandleUnitPositioningOnMap(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (focusPoint == null) return false;
//        if (unit.enemiesNear().groundUnits().havingWeapon().canAttack(unit, 3.5).atLeast(1)) return false;

//        if (
//            unit.isMissionAttack()
//                && !unit.isLeader()
//                && unit.enemiesNear().groundUnits().havingWeapon().canAttack(unit, 2.5).atLeast(1)
//        ) return false;

//        if (Count.ourCombatUnits() >= 6 && unit.isSquadScout()) return false;

//        if (A.now() >= 50 && unit.isDragoon()) {
//            System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " - .........");
//        }

//        if (focusPoint == null || !focusPoint.isValid()) {
////            System.out.println("invalid focusPoint = " + focusPoint);
//            return false;
//        }

//        if (unit.enemiesThatCanAttackMe(1.5).size() >= 2) return false;
//
//        if (unit.distToFocusPoint() <= 10) return true;
//        if (unit.isLeader()) return true;

        return true;
//        return unit.enemiesNear().inRadius(7, unit).empty();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            MakeSpaceForNearbyWorkers.class,
//            MakeSpaceForWrongSideOfFocusFriends.class,
//            OnWrongSideOfFocusPoint.class,

//            ProtossCohesion.class,

//            TooLonely.class,

//            AdvanceAsAlphaLeader.class,

            Cohesion.class,

            TooCloseToFocusPoint.class,
            TooFarFromFocusPoint.class,
        };
    }
}
