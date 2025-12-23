package atlantis.combat.running.low_eval;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossLowEval extends Manager {
    public ProtossLowEval(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (!unit.isCombatUnit()) return false;
//        if (!unit.isMissionAttack()) return false;
        if (A.isUms() && Count.bases() == 0) return false;

        double eval = unit.eval();

        if (unit.isMissionAttack()) {
            return eval <= 1.16 && wantsToApply();
        }

        return eval <= 0.95 && wantsToApply();
    }

    private boolean wantsToApply() {
        if (unit.isRanged() && EnemyInfo.noRanged()) {
            if (unit.meleeEnemiesNearCount(3) == 0) return false;
        }

        return true;
    }

    @Override
    public Manager handle() {
        HasPosition safety = unit.safetyPosition();
        if (safety == null) return null;
        if (unit.distTo(safety) <= (unit.isRanged() ? 6 : 1.5)) return null;

        if (unit.move(safety, Actions.RUN_ENEMIES)) {
            return usedManager(this);
        }

        return null;
    }
}
