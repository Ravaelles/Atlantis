package atlantis.combat.advance;

import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class Advance extends MissionManager {
    public Advance(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AdvanceAsTerran.class,
            AdvanceAsALeader.class,
            AdvanceStandard.class,
            WeDontKnowWhereEnemyIs.class,
        };
    }
}
