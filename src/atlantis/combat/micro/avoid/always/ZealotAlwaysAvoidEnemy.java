package atlantis.combat.micro.avoid.always;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ZealotAlwaysAvoidEnemy extends Manager {
    public ZealotAlwaysAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;

        if (Enemy.zerg()) return vsZerg();

        return false;
    }

    private boolean vsZerg() {
        if (dontFightIfNoCannonInRange()) return true;

        return false;
    }

    private boolean dontFightIfNoCannonInRange() {
        return unit.isMissionDefend()
            && Count.cannons() >= 1
            && unit.shieldWound() >= 3
            && A.s <= 60 * 6
//            && OurArmy.strength() <= 135
            && unit.friendsNear().cannons().inRadius(15, unit).notEmpty()
            && unit.friendsNear().cannons().inRadius(2.3, unit).empty();
    }
}
