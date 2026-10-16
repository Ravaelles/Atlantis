package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import atlantis.util.log.ErrorLog;

public class ReaverUseTransport extends Manager {

    private AUnit shuttle;
    private Selection enemies;

    public ReaverUseTransport(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (Count.shuttles() == 0) return false;
        if (
            unit.hp() >= 60
                && unit.lastActionLessThanAgo(60, Actions.UNLOAD)
                && !unit.shotSecondsAgo(2)
        ) return false;

        Selection enemiesNear = unit.enemiesNear();

        if (unit.isMoving() && (
            unit.distToTargetPosition() >= 15
                || (unit.hp() <= 160 && unit.enemiesThatCanAttackMe(2).atLeast(2))
        )) return true;

        enemies = enemiesNear.combatUnits().canAttack(unit, 6);

        if (Enemy.protoss() && enemiesNear.scarabs().countInRadius(5 + unit.woundPercent() / 33.0, unit) > 0) return true;

        if (dontInterruptAttack()) return false;

        if (
            unit.shieldWound() >= 20
                && (unit.shieldWound() >= 38 || unit.scarabCount() == 0)
                && unit.enemiesNearInRadius(4) > 0
                && enemies.nonBuildings().canAttack(unit, 2).notEmpty()
        ) return load("EnemiesClose");

        if (unit.lastActionLessThanAgo(20, Actions.LOAD)) return true;
        if (unit.lastUnderAttackLessThanAgo(50) && unit.shieldWound() >= 18) return load("UnderAttack");

        if (unit.hp() >= 120 && unit.shotSecondsAgo() >= 10 && enemies.groundUnits().canBeAttackedBy(unit, 4).notEmpty()) return false;

        if (
            unit.lastUnderAttackLessThanAgo(50) && (unit.shields() <= 40 || unit.shotSecondsAgo() <= 3)
        ) return load("UnderAttackAndWounded");
        if (justShootAndShouldEvacuate()) return load("Evacuate");

        if (unit.isAttackFrame()) return false;
        if (unit.isStartingAttack()) return false;

        if (loadDueToRunning()) return load("Running");
        if (againstTerranSiegeTanksImmediatelyPickUpAfterShot()) return load("QuickTanks");
        if (surroundedByEnemiesGetTheFuckOuttaHere()) return load("Surrounded");

        if (unit.lastActionLessThanAgo(45, Actions.UNLOAD) && !unit.shotSecondsAgo(2)) return false;

        if (
            unit.noCooldown()
                && unit.shotSecondsAgo() <= 4
                && unit.friendsInRadiusCount(3) >= 5
                && unit.shieldWound() <= 40
        ) return false;

        if (safeAgainstEnemiesAndHasTargets()) return false;

        if (unit.hp() >= 80 && unit.lastActionLessThanAgo(15, Actions.ATTACK_UNIT)) return false;

        if (
//            unit.shotSecondsAgo() > 4
            unit.enemiesNearInRadius(7.5) == 0
                && unit.enemiesNearInRadius(10) > 0
        ) return false;

        if (shouldGenericLoad()) return load("GenericNiceCar");

        return false;
    }

    private boolean shouldGenericLoad() {
        if (EnemyUnits.discovered().combatBuildingsAntiLand().count() == 0) return false;

        return (enemies.empty() && unit.enemiesNear().buildings().countInRadius(8, unit) == 0)
            || unit.hp() <= 80;
    }

    private boolean loadDueToRunning() {
        if (unit.isRunning() || unit.isAction(Actions.MOVE_AVOID)) {
            int nearbyEnemyCount = enemies.nonBuildings().countInRadius(12, unit);

            return nearbyEnemyCount > 0 && unit.shieldWound() >= 45 && unit.eval() <= 2;
        }

        return false;
    }

    private boolean load(String reason) {
//        System.err.println("Load reaver: " + reason);
        return true;
    }

    private boolean dontInterruptAttack() {
        if (
            unit.noCooldown()
                && unit.isActiveManager(ReaverContinueAttack.class)
                && unit.shields() >= 40
                && unit.distToTargetPosition() <= 10
                && unit.distToTargetPosition() >= 7.1
        ) {
            return true;
        }

        return false;
    }

    private boolean justShootAndShouldEvacuate() {
        return unit.cooldown() >= 10
            && unit.shieldWound() >= 45
            && unit.lastAttackFrameLessThanAgo(30)
            && unit.enemiesNear().nonBuildings().canAttack(unit, 1.1 + unit.woundPercent() / 30.0).atLeast(1);
    }

    private boolean againstTerranSiegeTanksImmediatelyPickUpAfterShot() {
        return Enemy.terran()
            && unit.shotSecondsAgo() <= 3
            && (unit.shieldWound() >= 35 || EnemyUnits.discovered().tanks().inRadius(AUnit.NEAR_DIST, unit).notEmpty());
    }

    private boolean surroundedByEnemiesGetTheFuckOuttaHere() {
        if (unit.shieldWound() <= 38 && unit.eval() >= 2) return false;

        Selection enemiesSuperNear = unit.enemiesNear().canAttack(unit, 2);

        return enemiesSuperNear.melee().atLeast(unit.hp() <= 180 ? 1 : 3)
            || enemiesSuperNear.ranged().nonBuildings().canAttack(unit, rangedEnemiesMargin()).atLeast(1);
    }

    private double rangedEnemiesMargin() {
        return 0.7 + unit.woundPercent() / 40.0;
    }

    private boolean safeAgainstEnemiesAndHasTargets() {
        return !Enemy.terran()
            && unit.enemiesNear().groundUnits().canAttack(unit, unit.shields() >= 40 ? 0.9 : 1.9).empty()
            && unit.enemiesNear().groundUnits().canBeAttackedBy(unit, 0.5).notEmpty();
    }

    @Override
    public Manager handle() {
        int minHp = 30;
        if (unit.enemiesNear().ranged().countInRadius(6, unit) >= 3) minHp = 50;

        shuttle = unit.friendsNear()
            .ofType(AUnitType.Protoss_Shuttle)
            .havingAtLeastHp(minHp)
            .havingSpaceFree(2)
            .nearestTo(unit);

        if (shuttle == null) {
//            ErrorLog.printMaxOncePerMinute("ReaverUseTransport: shuttle is null");
            return null;
        }
        if (!shuttle.hasFreeSpaceFor(unit)) {
            ErrorLog.printMaxOncePerMinute("ReaverUseTransport: no space for reaver");
            return null;
        }

        if (unit.load(shuttle)) {
            shuttle.load(unit);
            shuttle.setTooltip("Load reaver");
            return usedManager(this);
        }

        return null;
    }
}
