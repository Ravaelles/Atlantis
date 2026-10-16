package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
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

        double radius = radius(enemyDts);
        AUnit dt = enemyDts
            .inRadius(radius, unit)
            .effUndetected()
            .nearestTo(unit);
        if (dt == null) return null;

        if (unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!")) return usedManager(this);

        return null;
    }

    private double radius(Selection enemyDts) {
        if (unit.isWorker()) return 3;

        return Math.min(
            4.9,
            2.8 + enemyDts.count() - 1 + unit.woundPercent() / 40.0 + (unit.isWorker() ? 1.5 : 0)
        );
    }
}
