package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AsAirAttackAnyone::new,

            TerranContainEnemyWrapper::new,
            TerranAdvance::new,

//            AdvanceToAttackFocusPoint.class,

            OnWrongSideOfFocusPoint::new,

            ProtossCohesion::new,
            TerranCohesion::new,

            AttackNearbyEnemies::new,

//            ProtossCohesion.class,
//            TerranCohesion.class,

            TooFarFromFocusPoint::new,
        };
    }
}
