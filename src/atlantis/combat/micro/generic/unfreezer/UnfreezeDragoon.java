package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.PauseAndCenter;

public class UnfreezeDragoon extends Manager {
    public UnfreezeDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;
        if (unit.hasCooldown()) return false;
        if (unit.lastPositionChangedAgo() <= Unfreezer.UNFREEZE_WHEN_IDLE_FOR) return false;
        if (unit.lastActionLessThanAgo(5)) return false;
        if (unit.lastPositionChangedAgo() <= 70) return false;
//        if (unit.shotSecondsAgo(3)) return false;

//        boolean underAttackRecently = unit.lastUnderAttackLessThanAgo(15);
//        if (!underAttackRecently) return false;
//
//        if (unit.isLeader()) {
//            Squad squad = unit.squad();
//            if (squad != null) squad.changeLeader();
//        }

        return true;
    }

    @Override
    public Manager handle() {
//        System.out.println(unit + " history -----------------------------");
//        unit.commandHistory().print();
//        PauseAndCenter.on(unit);

        if (UnfreezerShakeUnit.shake(unit)) return yesUsedManager("UnfreezeD");

        return null;

//        if (FixActions.movedSlightlyOrToFocusPoint(unit)) return yesUsedManager("IdleAvoid-2Focus");
//        if (FixActions.attackEnemies(unit, this, 0.9)) return yesUsedManager("IdleAvoid-Attack");
//        if (FixActions.moveToLeader(unit)) return yesUsedManager("IdleAvoid-2Leader");

//        if (OldUnfreezerShake.shake(unit)) return usedManager(this);

//        return null;
    }

    private Manager yesUsedManager(String reason) {
        unit.setAction(Actions.UNFREEZE);

        return usedManager(this, reason);
    }
}
