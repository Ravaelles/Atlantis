package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.OnWrongSideOfFocusPoint;
import atlantis.combat.advance.focus.TooCloseToFocusPoint;
import atlantis.combat.advance.focus.TooFarFromFocusPoint;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.defend.protoss.sparta.ProtossSpartaSpecific;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
//import atlantis.combat.squad.positioning.protoss.ProtossSquadCohesion;
import atlantis.combat.squad.positioning.protoss.cohesion.ProtossCohesion;
import atlantis.combat.squad.positioning.protoss.cohesion.ProtossCohesionDuringDefend;
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

//            OldAdvance.class,

            OnWrongSideOfFocusPoint.class,

            AttackNearbyEnemies.class,

            ProtossCohesionDuringDefend.class,

            TooCloseToFocusPoint.class,
            TooFarFromFocusPoint.class,

//            AdvanceToDefendFocusPoint.class,
//            HandleUnitPositioningOnMap.class,
        };
    }
}
