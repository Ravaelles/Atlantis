package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.decions.Decision;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class MissionAttackPermissionToAttack extends Manager {

    public MissionAttackPermissionToAttack(AUnit unit) {
        super(unit);
    }

    public Decision permissionToAttack() {
        Squad squad = unit.squad();
        HasPosition squadCenter = unit.squadCenter();

        if (squad != null || squadCenter == null) {
            return Decision.INDIFFERENT;
        }

        int friendsCount = unit.friendsInRadiusCount(3);
        if (unit.outsideSquadRadius() && friendsCount <= 6) {
            if (unit.noCooldown() && unit.lastStartedAttackLessThanAgo(90) && friendsCount <= 4) {
                return Decision.FORBIDDEN;
            }
        }

        return Decision.INDIFFERENT;
    }
}
