package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
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
import atlantis.units.select.Select;
import atlantis.util.We;

public class DontAdvanceButHoldAndContainWhenEnemyBuildingsClose extends MissionManager {
    public static final int DIST_TO_ENEMY_MAIN_CHOKE = 10;

    private AChoke enemyMainChoke;
    private AChoke enemyNaturalChoke;
    private int tanks;

    public DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return appliesForTerran() || appliesForProtoss();
    }

    private boolean appliesForProtoss() {
        if (!We.protoss()) return false;

        return A.supplyUsed() < 190
            && A.minerals() < 2000
            && closeToEnemyBuildingsOrChoke();
    }

    private boolean appliesForTerran() {
        if (!We.terran()) return false;

        tanks = Count.tanks();

        return Count.ourCombatUnits() <= 25
            && (unit.isWounded() || (A.supplyUsed() < 185 && tanks <= 18))
            && A.minerals() < 2000
            && unit.isCombatUnit()
            && safeWithTanksSoSkip()
            && closeToEnemyBuildingsOrChoke();
    }

    private boolean safeWithTanksSoSkip() {
        if (unit.isTank()) return false;

        return unit.nearestOurTankDist() >= 3 && unit.friendsNear().inRadius(3, unit).atLeast(7);
    }

    protected Manager handle() {
//        System.err.println("@ " + A.now() + " - DontAdvanceButHoldAndContainWhenEnemyBuildingsClose " + unit);

        if (We.protoss()) return handleAsProtoss();
        if (We.terran()) return handleAsTerran();

        return null;
    }

    private Manager handleAsProtoss() {
        AUnit nearestEnemyBuilding = EnemyUnits.discovered().buildings().nearestTo(unit);

        if (nearestEnemyBuilding != null) {
            double dist = unit.distTo(nearestEnemyBuilding);

            if (dist <= 14) {
                unit.moveToMain(Actions.MOVE_FORMATION, "ContainOut");
                return usedManager(this);
            }
            if (dist > 16) {
                unit.move(nearestEnemyBuilding, Actions.MOVE_FORMATION, "ContainIn");
                return usedManager(this);
            }

            unit.holdPosition("ContainHold");
            return usedManager(this);
        }

        return null;
    }

    private Manager handleAsTerran() {
        if (unit.isTank()) return asTank();

        return asNonTank();
    }

    private Manager asNonTank() {
        if (unit.noCooldown() && unit.lastUnderAttackMoreThanAgo(30) && noEnemiesInShootRange()) {
//            unit.holdPosition("Steady");
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_FORMATION, "SteadyNow");
            return usedManager(this);
        }

        AUnit target = unit.nearestOurTank();
        if (target != null && unit.distTo(target) >= 4) {
            unit.move(target, Actions.MOVE_FORMATION, "CloserToTank");
            return usedManager(this);
        }

        return null;
    }

    private Manager asTank() {
        if (unit.isSieged() && unit.lastSiegedAgo() <= 30 * (24 + unit.id() % 6)) {
            if (unit.noCooldown() && unit.enemiesNear().groundUnits().inShootRangeOf(unit).notEmpty()) {
                (new AttackNearbyEnemies(unit)).invoke(this);
            }

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
        if (We.protoss()) return 15;

        return unit.isTank() ? 11.8 : 13.3;
    }
}
