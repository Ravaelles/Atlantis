package atlantis.combat.squad.positioning;

import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.terran.TerranInfantryComeCloser;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class ComeCloser extends MissionManager {
    public ComeCloser(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isGroundUnit() && focusPoint != null && (
            unit.friendsInRadius(1).groundUnits().atMost(1)
            && unit.friendsInRadius(2).groundUnits().atMost(5)
        )) {
            if (unit.isVulture()) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TooFarFromLeader.class,
            TerranInfantryComeCloser.class,
            TooFarFromSquadCenter.class,
        };
    }

    // =========================================================

//    private Manager comeCloser() {
//        AUnit friend = unit.squad().selection().exclude(unit).nearestTo(unit);
//        if (friend != null) {
//            /** Notice: Without .makeWalkable it is known to infinite crash Starcraft process for some reason... */
////            APosition goTo = friend.translateTilesTowards(0.3, unit).makeWalkable(3);
//            APosition goTo = friend.translateTilesTowards(0.3, unit).makeWalkable(1);
//            if (goTo != null) {
//                unit.move(goTo, Actions.MOVE_FORMATION, "Closer");
//                return usedManager(this);
//            }
//        }
//
//        return null;
//    }

    // =========================================================

//    private boolean shouldSkip() {
//        if (unit.lastActionLessThanAgo(13, Actions.MOVE_FORMATION)) {
//            return true;
//        }
//
//        if (Enemy.terran() && !We.terran()) {
//            return true;
//        }
//
//        AFocusPoint focusPoint = focusPoint();
//
//        if (focusPoint == null) {
//            return false;
//        }
//
//        if (unit.isAir()) {
//            return true;
//        }
//
//        if (unit.squad() == null) {
//            return true;
//        }
//
//        if (unit.isVulture()) {
//            return true;
//        }
//
//        if (Count.ourCombatUnits() <= 4) {
//            return true;
//        }
//
//        if (unit.meleeEnemiesNearCount(3) > 0) {
//            return true;
//        }
//
//        if (unit.enemiesNear().inRadius(6, unit).count() >= 5) {
//            return true;
//        }
//
//        return false;
//    }
}
