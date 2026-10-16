package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class TerranTooFarFromSquadCenter extends MissionManager {
    private HasPosition center;
    private AUnit nearestFriend;

    public TerranTooFarFromSquadCenter(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;

        if (unit.squad() == null || unit.isTank()) return false;
//        if (unit.isMissionAttackOrGlobalAttack()) return false;

//        if (unit.outsideSquadRadius()) return false;

        center = unit.squad().center();
        if (center == null) return false;

        if (!Enemy.terran() && !EnemyInfo.hasRanged()) return false;

        double maxDistToSquadCenter = unit.enemiesNear().empty() ? 10 : squad.radius();

        if (
            unit.distTo(center) > maxDistToSquadCenter
                && unit.friendsNear().inRadius(3.5, unit).atMost(7)
        ) {
            nearestFriend = unit.friendsNear().nearestTo(unit);

            if (nearestFriend == null) return false;

            Selection enemiesNear = unit.enemiesNear();
            if ((unit.isVulture() || unit.isDragoon()) && (enemiesNear.isEmpty() || enemiesNear.onlyMelee()))
                return false;

            return true;
        }

        return false;
    }

    @Override
    protected Manager handle() {
        if (applies()) {
            if (moveTowardsSquadCenter()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean moveTowardsSquadCenter() {
        return unit.move(
//            unit.translateTilesTowards(center, 2).makeWalkable(5),
            center,
            Actions.MOVE_FOCUS,
            "TooExposed(" + (int) center.distTo(unit) + "/" + (int) unit.distTo(nearestFriend) + ")",
            false
        );
    }
}
