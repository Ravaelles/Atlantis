package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class ProtossDontAvoidEnemy extends HasUnit {
    public ProtossDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
        if (!unit.isProtoss()) return false;

        if (ZealotDontAvoidEnemy.dontAvoid(unit)) return true;
        if (DragoonDontAvoidEnemy.dontAvoid(unit)) return true;

        return false;
    }
}
