package atlantis.combat.advance.old;

import atlantis.architecture.Manager;
import atlantis.combat.advance.terran.TerranAdvance;
import atlantis.combat.advance.contain.ContainEnemy;
import atlantis.combat.advance.focus.HandleFocusPointPositioning;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.micro.zerg.overlord.WeDontKnowEnemyLocation;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
import atlantis.combat.squad.positioning.terran.TerranCohesion;
import atlantis.combat.squad.positioning.too_lonely.TooLonely;
import atlantis.units.AUnit;

public class OldAdvance extends MissionManager {
    public OldAdvance(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            OldAdvanceAsALeader.class,

            AllowTimeToReposition.class,
//            ProtossSquadCohesion.class,
            TerranCohesion.class,

            ContainEnemy.class,

            AttackNearbyEnemies.class,

            TerranAdvance.class,

            TooLonely.class,

            AsAirAttackAnyone.class,

            HandleFocusPointPositioning.class,
            OldAdvanceAsALeader.class,
//            OldAdvanceStandard.class,

            WeDontKnowEnemyLocation.class,
        };
    }
}
