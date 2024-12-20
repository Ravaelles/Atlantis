package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.log.ErrorLog;

public class ReaverUseTransport extends Manager {

    private AUnit shuttle;

    public ReaverUseTransport(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.scarabCount() == 0 && unit.enemiesNear().canAttack(unit, 2).notEmpty()) return true;
        if (unit.lastActionLessThanAgo(20, Actions.LOAD)) return true;

        if (unit.lastUnderAttackLessThanAgo(50) && (unit.shields() <= 40 || unit.shotSecondsAgo() <= 3)) return true;
        if (unit.cooldown() >= 5 && unit.lastAttackFrameLessThanAgo(30)) return true;

        if (unit.isAttackFrame()) return false;
        if (unit.isStartingAttack()) return false;

        if (unit.isRunning() || unit.isAction(Actions.MOVE_AVOID)) return true;
        if (againstTerranSiegeTanksImmediatelyPickUpAfterShot()) return true;
        if (surroundedByEnemiesGetTheFuckOuttaHere()) return true;

        if (unit.lastActionLessThanAgo(35, Actions.UNLOAD)) return false;

        if (
            unit.noCooldown()
                && unit.shotSecondsAgo() <= 4
                && unit.friendsInRadiusCount(3) >= 5
                && unit.shieldWound() <= 40
        ) return false;

        if (safeAgainstEnemiesAndHasTargets()) return false;

//        System.err.println(unit.lastActionAgo(Actions.UNLOAD));
        if (unit.hp() >= 80 && unit.lastActionLessThanAgo(15, Actions.ATTACK_UNIT)) return false;

        if (
//            unit.shotSecondsAgo() > 4
            unit.enemiesNearInRadius(7.5) == 0
                && unit.enemiesNearInRadius(10) > 0
        ) return false;

        shuttle = unit.friendsNear()
            .ofType(AUnitType.Protoss_Shuttle)
            .havingAtLeastHp(30)
            .havingSpaceFree(2)
            .nearestTo(unit);

        return shuttle != null;
    }

    private boolean againstTerranSiegeTanksImmediatelyPickUpAfterShot() {
        return Enemy.terran()
            && unit.shotSecondsAgo() <= 3
            && (unit.shieldWound() >= 35 || EnemyUnits.discovered().tanks().inRadius(15, unit).notEmpty());
    }

    private boolean surroundedByEnemiesGetTheFuckOuttaHere() {
        Selection enemiesSuperNear = unit.enemiesNear().canAttack(unit, 2);

        return enemiesSuperNear.melee().atLeast(unit.hp() <= 180 ? 1 : 3)
            || enemiesSuperNear.ranged().canAttack(unit, rangedEnemiesMargin()).atLeast(1);
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
