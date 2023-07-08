package atlantis.combat.squad;

import atlantis.combat.squad.positioning.ComeCloser;
import atlantis.combat.squad.positioning.SquadTooLowCohesion;
import atlantis.combat.squad.positioning.TooBigToThinkOfCohesion;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class ASquadCohesionManager extends Manager {

//    private SquadCohesion squadCohesion;
//    private ComeCloser comeCloser;
//    private SquadTooLowCohesion squadTooLowCohesion;

    public ASquadCohesionManager(AUnit unit) {
        super(unit);
//        squadCohesion = new SquadCohesion(unit.squad());
//        comeCloser = new ComeCloser(unit);
//        squadTooLowCohesion = new SquadTooLowCohesion(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TooBigToThinkOfCohesion.class,
            ComeCloser.class,
            SquadTooLowCohesion.class,
        };
    }

    //    @Override
//    public Manager handle() {
////        if (shouldSkip()) {
////            return false;
////        }
//
//        if (comeCloser.handle() != null) {
//            return unit.manager();
//        }
//
////        if (TooClustered.handleTooClustered()) {
////            return true;
////        }
//
//        if (squadTooLowCohesion.handle() != null) {
//            return unit.manager();
//        }
//
//        return null;
//    }

//    private static boolean shouldSkip(AUnit unit) {
//        return
//                unit.isMissionDefend()
//                || unit.squadSize() <= 2
//                || unit.mission().focusPoint() == null
//                || (!unit.isMissionDefend() && unit.distToNearestChokeLessThan(6));
//    }

//    protected static AFocusPoint focusPoint(AUnit unit) {
//        return unit.mission().focusPoint();
//    }

//    private static boolean handleShouldStickCloser(AUnit unit) {
//        if (shouldSkipStickCloser()) {
//            return false;
//        }
//
//        Selection closeFriends = Select.ourCombatUnits().exclude();
//        AUnit nearestFriend = closeFriends.clone().nearestTo();
//        APosition center = squadCenter();
//
//        if (nearestFriend == null) {
//            return false;
//        }
//
//        if (isNearestFriendTooFar(unit, nearestFriend, center)) {
//            return true;
//        }
//
//        if (isTooFarFromSquadCenter(unit, nearestFriend, center)) {
//            return true;
//        }
//
//        if (isSquadQuiteNumerousAndUnitTooFarFromCenter(unit, nearestFriend, closeFriends)) {
//            return true;
//        }
//
//        return false;
//    }

    // =========================================================

//    protected static APosition squadCenter(AUnit unit) {
//        return unit.squad().center();
//    }

}
