package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;

public class LeaderProgressFlagToNextFocusChoke extends MissionManager {
    private int _lastProgressedAtS = -1;

    public LeaderProgressFlagToNextFocusChoke(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (A.supplyUsed() >= 170 || A.minerals() >= 1500) return true;

        AChoke focusChoke = CurrentFocusChoke.get();
//        System.err.println("A focusChoke = " + focusChoke + " / squadIsHere()=" + squadIsHere());
        if (focusChoke == null) return false;
        if (focusChoke.distTo(unit) >= 8) return false;

//        System.err.println("unit.combatEvalRelative() = " + unit.combatEvalRelative());

        return unit.enemiesNear().groundUnits().atMost(A.supplyUsed() >= 110 ? 4 : 2)
            && unit.lastStartedRunningMoreThanAgo(30 * 6)
            && unit.lastRetreatedAgo() >= 30 * 9
            && unit.noCooldown()
            && unit.combatEvalRelative() >= 2
            && !lastProgressedTooRecently()
            && squadIsHere();
    }

    @Override
    protected Manager handle() {
        if (CurrentFocusChoke.switchToNextIfPossible()) {
            _lastProgressedAtS = A.s;
            return usedManager(this, "LeaderProgressToNext");
        }

        return null;
    }

    private boolean squadIsHere() {
        double avgDistError = SquadDistanceToFocus.avgSquadDistErrorComparedToLeader(squad, unit, CurrentFocusChoke.get());

//        System.err.println("avgDistError = " + avgDistError);
        unit.setTooltip("AvgErr=" + A.digit(avgDistError));

        return avgDistError <= 3.3;
    }

    private boolean lastProgressedTooRecently() {
        return A.s - _lastProgressedAtS <= 30 * 4;
    }
}
