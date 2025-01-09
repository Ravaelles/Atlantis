package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class AvoidDT extends Manager {
    public AvoidDT(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Enemy.protoss() && unit.isGroundUnit() && !unit.isABuilding();
    }

    @Override
    protected Manager handle() {
        Selection enemyDts = unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar).inRadius(6, unit);
        if (enemyDts.empty()) return null;

//        if (unit.isMoving()) {
//            if (unit.isRunning() && (unit.runningFromUnit() != null && unit.runningFromUnit().isDT())) return null;
//            if (unit.lastActionLessThanAgo(2, Actions.RUN_ENEMY)) return null;
//        }

        double radius = Math.min(3.9, 2.7 + enemyDts.count() - 1 + unit.woundPercent() / 90.0);
        AUnit dt = enemyDts
            .inRadius(radius, unit)
            .effUndetected()
            .nearestTo(unit);
        if (dt == null) return null;

        if (unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!")) return usedManager(this);

        return null;
    }
}
