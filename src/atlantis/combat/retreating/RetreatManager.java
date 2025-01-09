package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossRetreat;
import atlantis.combat.retreating.terran.TerranRetreat;
import atlantis.units.AUnit;

public class RetreatManager extends Manager {
    public static int GLOBAL_RETREAT_COUNTER = 0;
//    private static Cache<Boolean> cache = new Cache<>();

    public RetreatManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            ContinueRetreating.class,
            TerranRetreat.class,
            ProtossRetreat.class,
        };
    }
}
