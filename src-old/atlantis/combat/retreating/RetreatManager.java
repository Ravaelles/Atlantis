package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class RetreatManager extends Manager {
    public static int GLOBAL_RETREAT_COUNTER = 0;
//    private static Cache<Boolean> cache = new Cache<>();

    public RetreatManager(AUnit unit) {
        super(unit);
    }
}
