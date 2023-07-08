package atlantis.combat.squad;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.positioning.ComeCloser;
import atlantis.combat.squad.positioning.SquadCohesion;
import atlantis.combat.squad.positioning.TooClustered;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.managers.Manager;

public class ASquadCohesionManager extends Manager {

    private ComeCloser comeCloser;
    private SquadCohesion squadCohesion;

    public ASquadCohesionManager(AUnit unit) {
        super(unit);
        comeCloser = new ComeCloser();
        squadCohesion = new SquadCohesion();
    }

    public Manager update(AUnit unit) {
//        if (shouldSkip()) {
//            return false;
//        }

        if (A.supplyUsed() >= 150 && ArmyStrength.ourArmyRelativeStrength() >= 50) {
            return null;
        }

        if (comeCloser.handleComeCloser() != null) {
            return unit.manager();
        }

//        if (TooClustered.handleTooClustered()) {
//            return true;
//        }

        if (squadCohesion.handleTooLowCohesion() != null) {
            return unit.manager();
        }

        return null;
    }

//    private static boolean shouldSkip(AUnit unit) {
//        return
//                unit.isMissionDefend()
//                || unit.squadSize() <= 2
//                || unit.mission().focusPoint() == null
//                || (!unit.isMissionDefend() && unit.distToNearestChokeLessThan(6));
//    }

    protected static AFocusPoint focusPoint(AUnit unit) {
        return unit.mission().focusPoint();
    }

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

    protected static APosition squadCenter(AUnit unit) {
        return unit.squad().center();
    }

}
