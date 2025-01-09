package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.defend.protoss.sparta.ProtossSpartaSpecific;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
//import atlantis.combat.squad.positioning.protoss.ProtossSquadCohesion;
import atlantis.units.AUnit;

public class MissionDefendManager extends MissionManager {
    public MissionDefendManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossSpartaSpecific.class,

            AllowTimeToReposition.class,
//            ProtossSquadCohesion.class,
            AdvanceToDefendFocusPoint.class,

//            OldAdvance.class,

            AttackNearbyEnemies.class,
        };
    }
}
