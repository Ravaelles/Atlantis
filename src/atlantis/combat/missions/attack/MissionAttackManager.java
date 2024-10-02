package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
//import atlantis.combat.squad.positioning.protoss.ProtossSquadCohesion;
import atlantis.combat.advance.AdvanceToAttackFocusPoint;
import atlantis.combat.advance.contain.ContainEnemy;
import atlantis.combat.advance.terran.TerranAdvance;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.terran.wraith.AsAirAttackAnyone;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
import atlantis.combat.squad.positioning.terran.TerranSquadCohesion;
import atlantis.combat.squad.positioning.too_lonely.TooLonely;
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

            AdvanceToAttackFocusPoint.class,

//            TooLonely.class,
        };
    }
}
