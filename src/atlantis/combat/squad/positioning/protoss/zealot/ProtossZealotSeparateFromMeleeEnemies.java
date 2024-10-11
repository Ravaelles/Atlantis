package atlantis.combat.squad.positioning.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossZealotSeparateFromMeleeEnemies extends Manager {
    private Selection enemiesNear;
    private boolean seriousWound;
    private int meleeEnemiesNear;

    public ProtossZealotSeparateFromMeleeEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;
        if (unit.lastUnderAttackMoreThanAgo(30 * 3)) return false;

        if (
            unit.isMissionDefendOrSparta()
                && unit.shieldWounded()
                && unit.cooldown() >= 7
        ) return true;

        seriousWound = unit.woundPercent() >= 10;
        if (
            (unit.shieldWounded() || unit.isMissionDefend() || unit.combatEvalRelative() <= 1)
                && (meleeEnemiesNear = unit.meleeEnemiesNearCount(distToEnemies())) >= minEnemies()
                && unit.friendsNear().nonBuildings().inRadius(1.5, unit).atMost(2)
        ) {
            return seriousWound || unit.cooldown() >= 7;
        }

        return false;
    }

    private int minEnemies() {
        if (Enemy.protoss()) return 1;
        return 3;
    }

    private double distToEnemies() {
        return seriousWound ? 4 : 1;
    }

    @Override
    protected Manager handle() {
        if (unit.enemiesNear().notEmpty()) {
            if (movedAway()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean movedAway() {
//        HasPosition centerOfEnemies = unit.enemiesNear().inRadius(5, unit).nearestTo(unit);
//        if (centerOfEnemies == null) centerOfEnemies = unit.enemiesNear().nearestTo(unit);
//
//        double moveDist = unit.distTo(centerOfEnemies) >= 2.5 ? 0.25 : 4;
//
////        return unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.RUN_ENEMY, "ZealotSeparate");

        if (unit.distToMain() >= 3) {
            return unit.moveToSafety(Actions.MOVE_AVOID, "ZealotSeparate");
        }

        return unit.runningManager().runFrom(
            unit.enemiesNear().nearestTo(unit), 3, Actions.RUN_ENEMY, unit.isMissionDefend()
        );
    }
}
