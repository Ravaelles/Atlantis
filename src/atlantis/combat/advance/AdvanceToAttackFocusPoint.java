package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.ContainEnemy;
import atlantis.combat.advance.focus.HandleFocusPointPositioning;
import atlantis.combat.advance.terran.TerranAdvance;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.micro.zerg.overlord.WeDontKnowEnemyLocation;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.too_lonely.TooLonely;
import atlantis.units.AUnit;

public class AdvanceToAttackFocusPoint extends MissionManager {
    public AdvanceToAttackFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AsAirAttackAnyone.class,

            ContainEnemy.class,

            AttackNearbyEnemies.class,

            TerranAdvance.class,

////            OldAdvanceAsALeader.class,
//
//            AllowTimeToReposition.class,
//            AttackNearbyEnemies.class,

//            TooLonely.class,
            HandleFocusPointPositioning.class,

            WeDontKnowEnemyLocation.class,
        };
    }
}
