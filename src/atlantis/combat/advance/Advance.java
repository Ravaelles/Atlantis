package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class Advance extends MissionManager {
    public Advance(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WeDontKnowWhereEnemyIs.class,
            AdvanceAsTerran.class,
            AdvanceAsALeader.class,
            AdvanceStandard.class,
        };
    }
}
