package atlantis.combat.squad.positioning.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

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
//        if (!Enemy.protoss()) return false;
        if (unit.cooldown() <= 5) return false;
        if (unit.cooldown() >= 5 && unit.shieldWounded()) return false;

        if (unit.lastStartedRunningLessThanAgo(3)) return false;
        if (unit.lastUnderAttackMoreThanAgo(30 * 3)) return false;
        if (unit.isMissionSparta()) return false;
        if (unit.shotSecondsAgo() >= 3) return false;

        if (
            unit.isMissionDefend()
                && unit.shieldWounded()
                && unit.cooldown() >= 7
        ) return true;

        seriousWound = unit.woundPercent() >= 10;
        if (
            (unit.shieldWounded() || unit.isMissionDefend() || unit.eval() <= 1)
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
                return usedManager(this, "ZealotSeparateA");
            }

            if (unit.moveToSafety(Actions.MOVE_AVOID)) {
                return usedManager(this, "ZealotSeparateB");
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

//        if (unit.distToMain() >= 3) {
//            return unit.moveToSafety(Actions.MOVE_AVOID, "ZealotSeparate");
//        }

        AUnit runFrom = unit.enemiesNear().combatUnits().melee().nearestTo(unit);
        if (runFrom == null) {
            return false;
        }

        return unit.runningManager().runFrom(
            runFrom, 4, Actions.RUN_ENEMY, unit.isMissionDefend()
        );
    }
}
