package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DanceAway extends Manager {
    protected AUnit enemy;
    protected Decision decision;

    public DanceAway(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return (new DanceAwayApplies(unit)).applies();
    }

    @Override
    public Manager handle() {
//        unit.paintCircleFilled(24, Color.Purple);

        if (enemy == null) enemy = DanceAwayApplies.defineUnitToDanceAwayFrom(unit);
        if (enemy == null) {
//            ErrorLog.printMaxOncePerMinute("DanceAway - enemy == null");
            return null;
        }

        String logString = "DanceAway-" + unit.cooldownRemaining();
        unit.addLog(logString);
//        System.err.println("@ " + A.now() + " - " + unit.id() + " - DANCE AWAY FROM " + awayFrom);

//        if (unit.isActionAttackUnit()) {
//            System.out.println(A.now + " ------- DANCE AWAY AS DRAGOON " + unit.action());
//            PauseAndCenter.on(unit);
//        }

//        System.err.println("unit.distTo(unit.targetPosition()) = " + unit.distTo(unit.targetPosition()));
//        if (unit.isMoving() && unit.isDancingAway() && unit.distTo(unit.targetPosition()) >= 0.2) {
        if (unit.isMoving() && unit.isDancingAway()) {
            return usedManager(this);
        }

        if (danceAwayFromTarget(logString)) {
//            unit.paintCircleFilled(18, Color.Teal);
            return usedManager(this);
        }

        return null;
//        return danceAwayError();
    }

    // =========================================================

    private boolean danceAwayFromTarget(String logString) {
        if (
            (unit.groundDistToMain() >= 50 || unit.friendsInRadiusCount(1) <= 2)
                && unit.moveToMain(Actions.MOVE_DANCE_AWAY)
        ) {
            unit.setTooltip("DanceToMain");
            return true;
        }

        return unit.runningManager().runFrom(
            enemy.position(), danceAwayDist(), Actions.MOVE_DANCE_AWAY, allowedToNotifyNearUnitsToMakeSpace()
        );
    }

    protected boolean allowedToNotifyNearUnitsToMakeSpace() {
        return false;
    }

    private double danceAwayDist() {
        return 2.2 + unit.woundPercent() / 40.0;
    }
}
