package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.Advance;
import atlantis.combat.micro.zerg.overlord.WeDontKnowEnemyLocation;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.defend.sparta.SpartaSpecific;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
import atlantis.combat.squad.positioning.MakeSpaceForNearbyWorkers;
import atlantis.combat.squad.positioning.MakeSpaceForWrongSideOfFocusFriends;
//import atlantis.combat.squad.positioning.protoss.ProtossSquadCohesion;
import atlantis.combat.squad.positioning.terran.TerranSquadCohesion;
import atlantis.units.AUnit;

public class MissionDefendManager extends MissionManager {
    public MissionDefendManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SpartaSpecific.class,
            AllowTimeToReposition.class,
//            ProtossSquadCohesion.class,
            AdvanceToDefendFocusPoint.class,
            WeDontKnowEnemyLocation.class,
            Advance.class,
        };
    }
}
