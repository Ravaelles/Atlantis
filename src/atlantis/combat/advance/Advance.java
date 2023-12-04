package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.advance.tank.AdvanceAsTank;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.squad.positioning.TooFarFromLeader;
import atlantis.units.AUnit;

public class Advance extends MissionManager {
    public Advance(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            WeDontKnowWhereEnemyIs.class,
            RetreatManager.class,
            DontAdvanceButHoldAndContainWhenEnemyBuildingsClose.class,
            AdvanceAsTerran.class,
            AdvanceAsALeader.class,
            AdvanceAsTank.class,
            TooFarFromLeader.class,
            AdvanceStandard.class,
            AsAirAttackAnyone.class,
        };
    }
}
