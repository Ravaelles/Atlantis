package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.TerranContainEnemyWrapper;
import atlantis.combat.advance.terran.TerranAdvance;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.micro.zerg.overlord.WeDontKnowEnemyLocation;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.protoss.cohesion.ProtossCohesion;
import atlantis.combat.squad.positioning.terran.TerranCohesion;
import atlantis.units.AUnit;

public class AdvanceToAttackFocusPoint extends MissionManager {
    public AdvanceToAttackFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossCohesion.class,
            TerranCohesion.class,

//            HandleUnitPositioningOnMap.class,

            AttackNearbyEnemies.class,

            WeDontKnowEnemyLocation.class,
        };
    }
}
