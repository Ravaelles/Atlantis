package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
//import atlantis.combat.squad.positioning.protoss.ProtossSquadCohesion;
import atlantis.combat.advance.AdvanceToAttackFocusPoint;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.units.AUnit;

public class MissionAttackManager extends Manager {
    public MissionAttackManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            AllowTimeToReposition.class,

//            ContainEnemy.class,
//
//            AttackNearbyEnemies.class,
//
//            TerranAdvance.class,

            AsAirAttackAnyone.class,
            AttackNearbyEnemies.class,

            AdvanceToAttackFocusPoint.class,

//            TooLonely.class,
        };
    }
}
