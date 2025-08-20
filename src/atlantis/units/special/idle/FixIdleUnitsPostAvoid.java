package atlantis.units.special.idle;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;

public class FixIdleUnitsPostAvoid extends Manager {
    public FixIdleUnitsPostAvoid(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isActiveManager(AvoidEnemies.class)) return false;
        if (unit.lastUnderAttackLessThanAgo(50)) return false;
        if (unit.lastPositionChangedAgo() <= 40) return false;
        if (unit.lastActionLessThanAgo(1)) return false;

        return (!unit.isMoving() || unit.enemiesThatCanAttackMe(2).empty())
            || unit.lastPositionChangedAgo() >= 2
            || unit.targetPosition() == null
            || unit.distToTarget() <= 0.05;
    }

    @Override
    public Manager handle() {
//        unit.paintCircleFilled(14, Color.Green);
//        System.err.println(A.now + " - " + unit.typeWithUnitId() + " - FixIdleUnitsPostAvoid");
//        GameSpeed.changeSpeedTo(50);
//        PauseAndCenter.on(unit);

        if (FixActions.attackEnemies(unit, this, 0.9)) return yesUsedManager("IdleAvoid-Attack");
        if (FixActions.moveToLeader(unit)) return yesUsedManager("IdleAvoid-2Leader");
        if (FixActions.movedSlightly(unit)) return yesUsedManager("IdleAvoid-2Focus");

        return null;
    }

    private Manager yesUsedManager(String message) {
//        System.err.println(
//            "Tlt:" + unit.tooltip() + " / "
//                + "Targ:" + unit.distToTargetPositionDigit() + " / "
//                + "Man:" + unit.manager() + " / "
//                + "Act:" + unit.action() + " / "
//                + "Comm:" + unit.lastCommandName() + " / "
//                + "PosChng:" + unit.lastPositionChangedAgo() + " / "
//        );

        return usedManager(this, message);
    }
}
