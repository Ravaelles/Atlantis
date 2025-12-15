package atlantis.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.game.player.Enemy;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ProtossZealotTooFarFromDragoon extends Manager {

//    private static final double MIN_DIST = 1.3;
    private static final double PREFERED_DIST = 1.5;

    private AUnit dragoon;
    private double distToGoon;

    public ProtossZealotTooFarFromDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;
        if (unit.isMissionSparta()) return false;
        if (Count.dragoons() <= 1) return false;
        if (A.supplyUsed() >= 180 || A.minerals() >= 1500) return false;
        if (unit.hp() <= 38 && unit.isRunning()) return false;
        if (Enemy.protoss() && A.now % 30 <= 10) return false;
//        if (unit.hp() >= 40 && unit.meleeEnemiesNearCount(2) >= 1) return false;

        dragoon = unit.friendsNear().dragoons().notRunning().nearestTo(unit);
        if (dragoon == null) return false;
        distToGoon = unit.distTo(dragoon);

        // === Force apply =========================================

        if (forceApply()) return true;

        // =========================================================

        if (Enemy.terran() && unit.enemiesNear().tanks().countInRadius(6, unit) > 0) return false;

        if (nearbyEnemyWorkers()) return false;
        if (unit.enemiesNear().empty() && unit.distToMain() <= 40) return false;

        double eval = unit.eval();
        if (eval >= 1.6 && distToGoon <= 3 && unit.shields() >= 10) return false;

        if (unit.enemiesNear().combatBuildings(true).countInRadius(3, unit) > 0) return false;

        // === First true ===========================================

        Decision decision;
        if ((decision = applyAgainstZerg()).notIndifferent()) return decision.toBoolean();
        if ((decision = applyAgainstTerran()).notIndifferent()) return decision.toBoolean();

        if (eval <= 4 && unit.enemiesNear().combatBuildingsAntiLand().notEmpty() && distToGoon >= 3) return true;

        if (distToGoon > absoluteMaxDist()) return true;
        if (beCautiousNearCB()) return true;

        if (distToGoon <= safeDist() && A.supplyUsed() >= 180) return false;

        if (unit.shieldWound() <= 17 && distToGoon <= safeDist()) {
            if (nearbyEnemiesAndGoodEval(distToGoon)) return false;
            if (distToGoon <= safeDist() && allowAttackingZergWhenRelativelyOk()) return false;
            if (distToGoon <= safeDist() && eval >= 1.7) return false;
        }

        if (
            eval >= 4
                && unit.shieldWound() <= 40
                && distToGoon <= absoluteMaxDist()
                && unit.enemiesNear().groundUnits().canBeAttackedBy(unit, 0.6).notEmpty()
        ) {
            return false;
        }

        if (
            eval <= 2 && distToGoon >= safeDist()
//                && !unit.enemiesNear().combatUnits().mostlyRanged()
        ) {
            return true;
        }

        return distToGoon >= preferedDist()
            && unit.enemiesNear().inRadius(4.2, unit).empty()
            && (
            unit.shieldWound() >= 16
                || unit.enemiesNear().combatBuildingsAntiLand().inRadius(8.3, unit).empty()
        );
    }

    private double preferedDist() {
        if (Enemy.zerg()) return PREFERED_DIST;

        return PREFERED_DIST + (unit.enemiesNear().ranged().canAttack(unit, 3.1).empty() ? 3 : 0);
    }

    private boolean forceApply() {
        double eval = unit.eval();
        if (eval >= 10) return false;
        if (
            eval >= 3 && distToGoon <= 3 && unit.shields() >= 5 && unit.enemiesICanAttack(1.7).notEmpty()
        ) return false;

        // When near choke
        if (distToGoon >= 1 && unit.nearestChokeDist() <= 3) {
            return true;
        }

        return false;
    }

    private Decision applyAgainstTerran() {
        if (!Enemy.terran()) return Decision.INDIFFERENT;

        if (unit.enemiesNear().tanks().countInRadius(6, unit) >= 1) {
            return Decision.TRUE("TankNearby");
        }

        if (distToGoon >= 1 && unit.eval() <= 6) {
            Selection vultures = unit.enemiesNear().vultures();
            if (vultures.countInRadius(6, unit) >= 1) {
                double nearestVultureDist = vultures.nearestToDist(unit);
                if (nearestVultureDist <= 5.5 && nearestVultureDist >= 1.2) {
                    return Decision.TRUE("VultureNearby");
                }
            }
        }

        return Decision.INDIFFERENT;
    }

    private Decision applyAgainstZerg() {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;

        if (distToGoon >= 0.8 && unit.shieldWound() >= 9 && unit.enemiesNear().hydras().countInRadius(6, unit) >= 1) {
            return Decision.TRUE("Z_Hydra");
        }

//        if (unit.hp() <= 42) return Decision.no();
//
//        int cooldown = unit.cooldown();
//        if (cooldown >= 12 && unit.hp() <= 102) return Decision.no();
//        if (cooldown >= 9 && unit.hp() <= 82) return Decision.no();
//
//        if (cooldown <= 7 && unit.hp() >= 62 && unit.eval() >= 0.9) return Decision.yes("Z1");
//        if (cooldown <= 6 && unit.meleeEnemiesNearCount(4) <= 2) return Decision.yes("Z2");

        return Decision.INDIFFERENT;
    }

    private double safeDist() {
        if (Enemy.protoss()) return 2.4;

        return 1.9;
    }

    private boolean nearbyEnemiesAndGoodEval(double distToGoon) {
        return unit.eval() >= 3
            && (distToGoon <= 3.8)
            && unit.enemiesNear().groundUnits().inRadius(3.5, unit).atLeast(2);
    }

    private boolean nearbyEnemyWorkers() {
        return unit.enemiesNear().workers().inRadius(6, unit).atLeast(3);
    }

    private double absoluteMaxDist() {
        if (Enemy.zerg()) return 2.7 - (unit.shields() <= 30 ? 1.2 : 0);

        return 4.2
            + (unit.eval() >= 1.5 ? 0.5 : 0)
            + (unit.meleeEnemiesNearCount(4) == 0 ? 1 : 0);
    }

    private boolean beCautiousNearCB() {
        return distToGoon >= preferedDist()
            && unit.enemiesNear().combatBuildingsAntiLand().countInRadius(10, unit) > 0;
    }

    private boolean allowAttackingZergWhenRelativelyOk() {
        if (!Enemy.zerg()) return false;

        return unit.cooldown() <= 4
            && unit.eval() >= 0.75
            && unit.hp() >= 60
            && unit.meleeEnemiesNearCount(1.2) <= 1;
    }

    @Override
    protected Manager handle() {
        if (distToGoon <= 0.4) {
            if (unit.moveAwayFrom(dragoon, 0.2, Actions.MOVE_FORMATION)) {
                return usedManager(this, "TooCloseToGoon");
            }
        }

        if (moveTo()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean moveTo() {
        if (!dragoon.isWalkable()) {
            return false;
        }

        return unit.move(dragoon, Actions.MOVE_FORMATION, "TooFarFromGoon");
    }
}
