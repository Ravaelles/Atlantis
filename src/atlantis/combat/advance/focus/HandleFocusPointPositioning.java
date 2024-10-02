package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.advance.leader.AdvanceAsAlphaLeader;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class HandleFocusPointPositioning extends MissionManager {
    public HandleFocusPointPositioning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (Count.ourCombatUnits() >= 6 && unit.isSquadScout()) return false;

//        if (A.now() >= 50 && unit.isDragoon()) {
//            System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " - .........");
//        }

        if (focusPoint == null || !focusPoint.isValid()) {
//            System.out.println("invalid focusPoint = " + focusPoint);
            return false;
        }

        if (unit.enemiesThatCanAttackMe(1.5).size() >= 2) return false;

        if (unit.distToFocusPoint() <= 10) return true;
        if (unit.isLeader()) return true;

        return false;
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

            AdvanceAsAlphaLeader.class,

            TooCloseToFocusPoint.class,
            TooFarFromFocusPoint.class,
        };
    }
}
