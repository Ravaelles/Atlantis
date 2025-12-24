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
        if (unit.isRunningOrRetreating()) return false;
//        if (!unit.isMissionAttack()) return false;
        if (A.isUms() && Count.bases() == 0) return false;

        if (unit.cooldown() == 0 && unit.ourBuildingsNearCount(4) > 0) {
            return false;
        }

        double eval = evalWithPenalties();

        if (unit.isMissionAttack()) {
            return eval <= 1.16 && wantsToApply();
        }

        return eval <= 0.95 && wantsToApply();
    }

    private boolean wantsToApply() {
        if (unit.cooldown() <= 4 && unit.isRanged() && EnemyInfo.noRanged()) {
            if (unit.meleeEnemiesNearCount(unit.groundWeaponRange() - 0.5) == 0) return false;
        }

        return true;
    }

    @Override
    public Manager handle() {
        HasPosition safety = unit.safetyPosition();
        if (safety == null) return null;
        if (unit.distTo(safety) > (unit.isRanged() ? 8 : 5)) {
            if (unit.move(safety, Actions.RUN_ENEMIES)) {
                return usedManager(this);
            }
        }

        AUnit enemy = unit.nearestEnemy();
        if (enemy == null) return null;

        if (unit.runOrMoveAway(enemy, 5)) {
            unit.setAction(Actions.RUN_ENEMIES);
            return usedManager(this);
        }

        return null;
    }

    private double evalWithPenalties() {
        return unit.eval()
            + (new ProtossLowEvalChokeTweaks(unit)).evalChokePenalty();
    }
}
