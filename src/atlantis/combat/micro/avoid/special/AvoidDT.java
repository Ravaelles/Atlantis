package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

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
        Selection enemyDts = unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar);
        if (enemyDts.empty()) return null;

        if (unit.isMoving()) {
            if (unit.isRunning() && (unit.runningFromUnit() != null && unit.runningFromUnit().isDT())) return null;
            if (unit.lastActionLessThanAgo(6, Actions.RUN_ENEMY)) return null;
        }

        AUnit dt = enemyDts
            .inRadius(2.6 + unit.woundPercent() / 80.0, unit)
            .effUndetected()
            .nearestTo(unit);
        if (dt == null) return null;

        if (unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!")) return usedManager(this);

        return null;
    }
}
