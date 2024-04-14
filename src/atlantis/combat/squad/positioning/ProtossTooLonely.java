package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;
import atlantis.util.We;
import sun.security.jgss.krb5.Krb5NameElement;

public class ProtossTooLonely extends Manager {
    private AUnit leader;

    public ProtossTooLonely(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (unit.isAir()) return false;
        if (unit.isDT()) return false;
        if (unit.distToNearestChokeLessThan(5)) return false;

        return isTooLonely();
    }

    private boolean isTooLonely() {
        return unit.friendsNear().inRadius(2.5, unit).empty();
    }

    protected Manager handle() {
        if (leader == null) leader = unit.squadLeader();

        if (!unit.isMoving() || A.everyNthGameFrame(5)) {
            if (unit.move(leader, Actions.MOVE_FORMATION, "Coordinate")) {
                return usedManager(this);
            }
        }

        return null;
    }
}
