package atlantis.combat.advance.leader;

import atlantis.combat.squad.Squad;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class SquadDistanceToFocus {
    public static double avgSquadDistErrorComparedToLeader(Squad squad, AUnit leader, AChoke focus) {
        APosition focusCenter = focus.center();
        double leaderToFocus = leader.distTo(focusCenter);
        double totalSquadDistToFocus = 0;

        for (AUnit unit : squad.list()) {
            totalSquadDistToFocus += unit.distTo(focusCenter);
        }
        double avgSquadDistToFocus = totalSquadDistToFocus / squad.size();

        return Math.abs(avgSquadDistToFocus - leaderToFocus);
    }
}
