package atlantis.combat.squad.positioning.protoss.cohesion;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

/**
 * Move combat units closer one to another.
 */
public class ProtossCombat2Combat extends Manager {

    private Selection friends;

    public ProtossCombat2Combat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
        if (unit.effUndetected()) return false;
        if (unit.isMissionSparta()) return false;
        if (unit.hp() <= 40) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.isDancingAway()) return false;
        if (unit.squadSize() <= 2) return false;
        if (unit.lastStoppedRunningLessThanAgo(25)) return false;

        if (unit.isAttacking() && unit.lastCommandIssuedAgo() <= 15) return false;
        if (unit.shotSecondsAgo(3) && unit.eval() >= 5) return false;
//        if (unit.cooldown() <= 8 && unit.enemiesNear().countInRadius(4, unit) >= 1) return false;

        int minFriendsNear = (unit.squadSize() >= 4 ? 2 : 1) + (unit.cooldown() >= 12 ? 1 : 0);
        double biggerRadius = unit.enemiesNear().combatUnits().empty() ? 2.5 : 1.3;
        double smallerRadius = 0.5;

        friends = friends();
        Selection inBiggerRadius = friends.inRadius(biggerRadius, unit);
        if (inBiggerRadius.atLeast(minFriendsNear)) return false;

//        if (unit.squadSize() >= 4) {
//        } else {
//            if (unit.friendsNear().combatUnits().inRadius(2, unit).atLeast(2)) return false;
//        }

        if (unit.nearestChokeCenterDist() <= 4) return false;

        if (inBiggerRadius.inRadius(smallerRadius, unit).empty()) return true;

        return false;
    }

    @Override
    public Manager handle() {
        AUnit friend = friendToGoTo();

        if (
            friend != null
                && friend.isWalkable()
                && unit.distTo(friend) >= 0.5
                && unit.move(friend, Actions.MOVE_FORMATION, "CombatToCombat")
        ) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit friendToGoTo() {
        AUnit leader = unit.squadLeader();
        if (leader != null && unit.distTo(leader) > 10) {
            return leader;
        }

        return friends().havingAtLeastHp(35).nearestTo(unit);
    }

    private Selection friends() {
        if (friends == null) {
            friends = unit.friendsNear().combatUnits().groundUnits();
        }

        return friends;
    }
}
