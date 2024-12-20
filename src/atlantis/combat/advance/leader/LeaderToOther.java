package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class LeaderToOther extends MissionManager {
    private AUnit otherFriend;

    public LeaderToOther(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        otherFriend = otherFriend();
        if (otherFriend == null) return false;

        double distToOther = otherFriend.distTo(unit);

        return distToOther <= 20 && distToOther > dist();
    }

    private double dist() {
        if (unit.enemiesNear().combatUnits().havingWeapon().notEmpty()) return 0.75;

        return 3 + unit.squadSize() / 4.0 + (OurArmy.strength() >= 200 ? 2 : 0);
    }

    @Override
    protected Manager handle() {
        if (unit.isOvercrowded()) return null;
        if (!otherFriend.isWalkable()) return null;

        if (unit.move(otherFriend, Actions.MOVE_FORMATION, "LeaderToOther")) return usedManager(this);

        return null;
    }

    private AUnit otherFriend() {
        return unit.squad().units().groundUnits().exclude(unit).combatUnits().nearestTo(unit);
    }
}
