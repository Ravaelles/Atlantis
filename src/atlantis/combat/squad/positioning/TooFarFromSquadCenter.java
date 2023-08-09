package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class TooFarFromSquadCenter extends MissionManager {
    public TooFarFromSquadCenter(AUnit unit) {
        super(unit);
    }

    protected Manager handle() {
        if (isTooFarFromSquadCenter()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean isTooFarFromSquadCenter() {
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
}
