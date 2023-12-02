package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.ForceSiege;
import atlantis.combat.micro.terran.tank.sieging.WantsToSiege;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class DontAdvanceButHoldAndContainWhenEnemyBuildingsClose extends MissionManager {
    public static final int DIST_TO_ENEMY_MAIN_CHOKE = 8;

    private AChoke enemyMainChoke;
    private AChoke enemyNaturalChoke;
    private int tanks;

    public DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        tanks = Count.tanks();

        return
            (A.supplyUsed() < 185 || tanks <= 10)
                && A.minerals() < 2000
                && unit.isCombatUnit()
                && closeToEnemyBuildingsOrChoke();
    }

    protected Manager handle() {
//        System.err.println("@ " + A.now() + " - DontAdvanceButHoldAndContainWhenEnemyBuildingsClose " + unit);

        if (unit.isTank()) return asTank();

        return asNonTank();
    }

    private Manager asNonTank() {
        if (unit.noCooldown() && unit.lastUnderAttackMoreThanAgo(30) && noEnemiesInShootRange()) {
            unit.holdPosition("Steady");
            return usedManager(this);
        }

        if (unit.nearestOurTankDist() >= 4) {
            unit.move(unit.nearestOurTank(), Actions.MOVE_FORMATION, "CloserToTank");
            return usedManager(this);
        }

        return null;
    }

    private Manager asTank() {
        if (unit.isSieged() && unit.lastSiegedAgo() <= 30 * (17 + unit.id() % 6)) {
            return usedManager(this, "StayHere");
        }

        if (unit.isTankUnsieged()) {
            if (unit.enemiesNearInRadius(10) == 0) {
                ForceSiege.forceSiegeNow(this, "RemainHere");
    //            WantsToSiege.wantsToSiegeNow(unit, "RemainHere");
                return usedManager(this);
            }
        }

        return null;
    }

    // =========================================================

    private boolean noEnemiesInShootRange() {
        return unit.enemiesNear().canBeAttackedBy(unit, 1.5).empty();
    }

    private boolean closeToEnemyBuildingsOrChoke() {
        return EnemyUnits.discovered().buildings().inRadius(minDistToEnemyBuilding(), unit).notEmpty()
            && closeToEnemyChokes();
    }

    private boolean closeToEnemyChokes() {
        return
            (
                (enemyMainChoke = Chokes.enemyMainChoke()) != null
                    && enemyMainChoke.distTo(unit) < DIST_TO_ENEMY_MAIN_CHOKE
            )
                ||
                (
                    (enemyNaturalChoke = Chokes.enemyNaturalChoke()) != null
                        && enemyNaturalChoke.distTo(unit) < 5
                )
            ;
    }

    private double minDistToEnemyBuilding() {
        return unit.isTank() ? 10.7 : 13.3;
    }
}
