package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.terran.TerranTooFarFromLeader;
import atlantis.units.AUnit;

public class Advance extends MissionManager {
    public Advance(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            AdvanceAsALeader.class,

            DontAdvanceButHoldAndContainWhenEnemyBuildingsClose.class,

            AttackNearbyEnemies.class,

            AdvanceAsTerran.class,
            AdvanceStandard.class,
            AsAirAttackAnyone.class,
        };
    }
}
