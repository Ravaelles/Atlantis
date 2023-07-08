package atlantis.combat.squad.positioning;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ComeCloser extends Manager {

    private ComeCloserToTanks comeCloserToTanks;
    private TerranInfantryComeCloser terranInfantryComeCloser;

    public ComeCloser(AUnit unit) {
        super(unit);
//        comeCloserToTanks = new ComeCloserToTanks();
//        terranInfantryComeCloser = new TerranInfantryComeCloser();
    }

    @Override
    public Manager handle() {
        if (shouldSkip()) {
            return null;
        }

        if (isTooFarFromSquadCenter() || isTooFarAhead()) {
            return comeCloser();
        }

        if (terranInfantryComeCloser.handleTooFarFromMedic() != null) return lastManager();

        if (comeCloserToTanks.handleTooFarFromTanks() != null) return lastManager();

        return null;
    }

    // =========================================================

    private Manager comeCloser() {
//        if (unit.isMoving()) {
//            unit.setTooltip("Closer...");
//            return true;
//        }

        AUnit friend = unit.squad().selection().exclude(unit).nearestTo(unit);
        if (friend != null) {
            /** Notice: Without .makeWalkable it is known to infinite crash Starcraft process for some reason... */
//            APosition goTo = friend.translateTilesTowards(0.3, unit).makeWalkable(3);
            APosition goTo = friend.translateTilesTowards(0.3, unit).makeWalkable(1);
            if (goTo != null) {
                unit.move(goTo, Actions.MOVE_FORMATION, "Closer");
                return usedManager(this);
            }
        }

        return null;
    }

    // =========================================================

    private boolean isTooFarAhead() {
//        if (unit.isMoving() && unit.lastActionLessThanAgo(6)) {
//            return false;
//        }

        if (unit.cooldownRemaining() == 0) {
            return false;
        }

        if (unit.distToLeader() <= 4 || unit.distToLeader() >= 15) {
            return false;
        }

        AFocusPoint focusPoint = focusPoint();
        if (
            focusPoint != null
                && unit.groundDist(focusPoint) + 3 <= unit.squad().groundDistToFocusPoint()
        ) {
            if (unit.friendsNear().inRadius(4, unit).atMost(5)) {
                if (unit.isMoving() && unit.cooldown() == 0) {
                    unit.addLog("TooAhead");
                    return unit.holdPosition("TooAhead");
                }
            }
        }

        return false;
    }

    private boolean shouldSkip() {
        if (unit.lastActionLessThanAgo(13, Actions.MOVE_FORMATION)) {
            return true;
        }

        if (Enemy.terran() && !We.terran()) {
            return true;
        }

        AFocusPoint focusPoint = focusPoint();

        if (focusPoint == null) {
            return false;
        }

        if (unit.isAir()) {
            return true;
        }

        if (unit.squad() == null) {
            return true;
        }

        if (unit.isVulture()) {
            return true;
        }

        if (Count.ourCombatUnits() <= 4) {
            return true;
        }

        if (unit.meleeEnemiesNearCount(3) > 0) {
            return true;
        }

        if (unit.enemiesNear().inRadius(6, unit).count() >= 5) {
            return true;
        }

        return false;
    }

    // =========================================================

    public boolean isTooFarFromSquadCenter() {
        if (unit.squad() == null || unit.isTank()) {
            return false;
        }

        if (unit.isMissionAttack()) {
            return false;
        }

//        if (unit.distToSquadCenter() >= 15) {
//            return false;
//        }

        APosition center = unit.squad().center();
        if (center == null) {
            return false;
        }

        double maxDistToSquadCenter = squad.radius();

        if (unit.distTo(center) > maxDistToSquadCenter && unit.friendsNear().inRadius(3.5, unit).atMost(7)) {
            AUnit nearestFriend = unit.friendsNear().nearestTo(unit);
            if (nearestFriend == null) {
                return false;
            }

            Selection enemiesNear = unit.enemiesNear();
            if ((unit.isVulture() || unit.isDragoon()) && (enemiesNear.isEmpty() || enemiesNear.onlyMelee())) {
                return false;
            }

            if (unit.move(
                unit.translateTilesTowards(center, 2).makeWalkable(5),
                Actions.MOVE_FOCUS,
                "TooExposed(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")",
                false
            )) {
                unit.addLog("TooExposed");
                return true;
            }
        }

        return false;
    }

    protected AFocusPoint focusPoint() {
        return unit.mission().focusPoint();
    }

}
