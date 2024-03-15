package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossRetreat;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;
import bwapi.Color;

public class RetreatManager extends Manager {
    public static int GLOBAL_RETREAT_COUNTER = 0;
//    private static Cache<Boolean> cache = new Cache<>();

    public RetreatManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ContinueRetreating.class,
            ProtossRetreat.class,
        };
    }
}
