package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ReaverForceFollowAnotherCombatUnit extends Manager {
    private AUnit nearestFriend;

    public ReaverForceFollowAnotherCombatUnit(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        nearestFriend = unit.friendsNear().combatUnits().nonReavers().nearestTo(unit);
        if (nearestFriend == null) return false;

        double groundDistToMain = unit.groundDistToMain();
        if (groundDistToMain <= 35 || groundDistToMain < (Alpha.groundDistToMain() * 0.66)) return false;

        Selection enemiesICanAttack = unit.enemiesICanAttack(3);
        if (enemiesICanAttack.notEmpty() && unit.cooldown() <= 10) return false;

        return unit.distTo(nearestFriend) >= maxDist()
            && (unit.hp() <= 100 || unit.cooldown() >= 13 || enemiesICanAttack.empty());
    }

    private double maxDist() {
        if (unit.friendsNear().cannons().countInRadius(3, unit) > 0) return 9;

        return 0.5 + unit.hpPercent() / 35.0;
    }

    @Override
    public Manager handle() {
        if (nearestFriend != null && nearestFriend.isWalkable() && unit.move(nearestFriend, Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        return null;
    }
}
