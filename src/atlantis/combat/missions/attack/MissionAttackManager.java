package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
//import atlantis.combat.squad.positioning.protoss.ProtossSquadCohesion;
import atlantis.combat.advance.AdvanceToAttackFocusPoint;
import atlantis.combat.advance.contain.TerranContainEnemyWrapper;
import atlantis.combat.advance.focus.OnWrongSideOfFocusPoint;
import atlantis.combat.advance.focus.TooFarFromFocusPoint;
import atlantis.combat.advance.terran.TerranAdvance;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.squad.positioning.protoss.cohesion.ProtossCohesion;
import atlantis.combat.squad.positioning.terran.TerranCohesion;
import atlantis.units.AUnit;

public class MissionAttackManager extends Manager {
    public MissionAttackManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AsAirAttackAnyone.class,

            TerranContainEnemyWrapper.class,
            TerranAdvance.class,

//            AdvanceToAttackFocusPoint.class,

            OnWrongSideOfFocusPoint.class,

            ProtossCohesion.class,
            TerranCohesion.class,

            AttackNearbyEnemies.class,

//            ProtossCohesion.class,
//            TerranCohesion.class,

            TooFarFromFocusPoint.class,
        };
    }
}
