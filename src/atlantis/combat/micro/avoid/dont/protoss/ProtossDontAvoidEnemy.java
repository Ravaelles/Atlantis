package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class ProtossDontAvoidEnemy extends HasUnit {
    public ProtossDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    //    @Override
    public boolean applies() {
        if (!unit.isProtoss()) return false;

//        if (unit.looksIdle() || unit.lastActionMoreThanAgo(40)) return true;
        if (DragoonDontAvoidEnemy.dontAvoid(unit)) return true;
        if (ObserverDontAvoidEnemy.dontAvoid(unit)) return true;

        return false;
    }

//    @Override
//    public Manager handle() {
//        return null; // Avoid returning non-null here as units may get stuck thinking they got an order
//    }
}
