package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class AvoidCombatBuildingClose extends Manager {
    public static final double MIN_UNSAFE = 8.1;
    public static final double MIN_SAFE = 9.5;
    private AUnit combatBuilding;
    private Selection ourCombatUnitsNearby;
    private double dist;

    public AvoidCombatBuildingClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isRunning()) return false;

        int heroModeAtMinerals = 1100;
        if (A.supplyUsed() >= 170 && A.hasMinerals(heroModeAtMinerals)) return false;
        if (A.hasMinerals(heroModeAtMinerals) && A.s >= 60 * 15) return false;

        combatBuilding = combatBuilding();
        if (combatBuilding == null) return false;
        if (unit.enemiesNear().nonBuildings().canAttack(unit, 1.2).notEmpty()) return false;

        dist = unit.distTo(combatBuilding);

        if (fiercelyEngageEnemyThirdOrLaterBase()) return false;
//        if (unit.distToSquadCenter() > 20) return false;
        if (unit.friendsNear().combatUnits().atLeast(20)) return false;

//        if (unit.lastUnderAttackLessThanAgo(30 * 2)) return false;
//        if (unit.combatEvalRelative() >= 2.5) return false;

        return combatBuildingShouldNotBeEngaged()
            && ifLurkersNearbyDontAvoidTheBuilding()
            && !strongEnoughToAttack();
    }

    @Override
    protected Manager handle() {
        if (shouldHoldGround(combatBuilding)) {
//            System.err.println("@ " + A.now() + " - HANDLE A - " + unit.typeWithUnitId());
//            unit.holdPosition("HoldHere" + A.dist(dist));
            unit.holdPosition("HoldHere" + unit.combatEvalRelativeDigit());
            return usedManager(this);
        }

//        if (!unit.isAttacking() && (!unit.isMoving() || A.fr % 15 == 0)) {
//            System.err.println("@ " + A.now() + " - HANDLE B - " + unit.typeWithUnitId());
//            AUnit moveTo = Select.mainOrAnyBuilding();

//            if (moveTo != null) unit.move(moveTo, Actions.MOVE_AVOID, "HoldCB");
//        unit.moveAwayFrom(combatBuilding, moveAwayDist(), Actions.MOVE_AVOID, "AvoidCB" + A.dist(dist));
        unit.moveAwayFrom(combatBuilding, moveAwayDist(), Actions.MOVE_AVOID, "AvoidCB" + unit.combatEvalRelativeDigit());
        return usedManager(this);
//        }
//
//        unit.setTooltip("WearyCB");
//        return usedManager(this);
    }

    private boolean fiercelyEngageEnemyThirdOrLaterBase() {
        if (A.supplyUsed() < 35 || unit.friendsNear().combatUnits().atMost(5)) return false;

        APosition enemyNatural = EnemyInfo.enemyNatural();
        if (enemyNatural == null) return false;
        if (enemyNatural.distTo(unit) <= 18) return false;

        APosition enemyMain = EnemyInfo.enemyMain();
        if (enemyMain == null) return false;
        if (enemyMain.distTo(unit) <= 18) return false;

//        APosition enemyThird = BaseLocations.enemyThird();
//        if (enemyThird == null) return false;
//        if (unit.distTo(enemyThird) >= 14) return false;

        return true;
    }

    private boolean ifLurkersNearbyDontAvoidTheBuilding() {
        return unit.enemiesNear().lurkers().inRadius(8.2, unit).empty();
    }

    private boolean strongEnoughToAttack() {
        if ((A.supplyUsed() >= 194 || A.minerals() >= 2000) && unit.squadSize() >= 10) return true;

//        if (unit.combatEvalRelative() < 1) return false;
        if (ProtossEvaluateAgainstCombatBuildings.chancesLookGood(unit, combatBuilding)) return true;
//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.combatEvalRelative());

//        if (unit.combatEvalRelative() <= (2.7 - (A.supplyUsed() > 185 ? 1 : 0))) return false;

        Selection enemyBuildings = unit.enemiesNear().combatBuildingsAnti(unit);
        if (unit.friendsNear().combatUnits().count() * 6 >= enemyBuildings.count()) {
            if (unit.friendsNear().totalHp() >= enemyBuildings.totalHp()) {
//                return unit.combatEvalRelative() >= 1.5;
                return true;
            }
        }

        return false;
    }

    private double moveAwayDist() {
        if (dist <= 6) return 5;
        if (dist <= 7) return 2;
        if (dist <= 8) return 0.5;

        return dist > MIN_UNSAFE && unit.hpPercent(40) ? 0.1 : 0.4;
    }

    private boolean shouldHoldGround(AUnit combatBuilding) {
        if (unit.lastUnderAttackLessThanAgo(60)) return false;

        if (
            dist >= 7.7 && dist <= 8.1
                && Count.ourCombatUnits() >= 10
                && unit.friendsInRadiusCount(1) >= 1) {
            return true;
        }

        if (dist <= MIN_UNSAFE) return false;
        if (unit.enemiesNear().nonBuildings().canAttack(unit, 1.3).notEmpty()) return false;

//        double minDistAllowed = (unit.isRanged() ? MIN_SAFE : (MIN_SAFE + 1.5)) ;

//        if (
//            dist <= minDistAllowed && (
//                !unit.isAttacking() || !unit.isTargetInWeaponRangeAccordingToGame()
//            )
//        ) return true;

        return dist >= (MIN_SAFE + (unit.woundPercent() / 40.0));
//        return unit.isMoving()
//            && unit.targetPosition() != null
//            && dist < minDistAllowed;
    }

    // =========================================================


    private AUnit combatBuilding() {
        return unit.enemiesNear()
            .buildings()
            .onlyCompleted()
            .combatBuildingsAnti(unit)
            .inRadius(10, unit)
            .nearestTo(unit);
    }

    private boolean combatBuildingShouldNotBeEngaged() {
        ourCombatUnitsNearby = combatBuilding.enemiesNear().combatUnits();

        if (unit.combatEvalRelative() >= 2 || ourCombatUnitsNearby.count() >= 15) return false;

        if (dontEngageBecauseTooManyEnemyCombatUnitsNearby()) return true;

//        AUnit ourUnit = ourCombatUnitsNearby.nearestTo(combatBuilding);
//        System.err.println("ourUnit.combatEvalRelative() = " + ourUnit.combatEvalRelative());
//        System.err.println("ourUnit.combatEvalAbs() = " + ourUnit.combatEvalAbsolute());
        return unit != null
//            && ourCombatUnitsNearby.count() <= 4
            && (
            unit.combatEvalRelative() <= 2.5 || ourCombatUnitsNearby.atMost(4)
//                && ourUnit.combatEvalAbsolute() <= -200
        );
    }

    private boolean dontEngageBecauseTooManyEnemyCombatUnitsNearby() {
        if (A.supplyUsed() >= 170 || A.hasMinerals(3000)) return false;

        if (unit.friendsNear().combatUnits().atLeast(25)) return false;

        return combatBuilding.enemiesNear()
            .combatUnits()
            .inRadius(6, combatBuilding).atMost((int) (ourCombatUnitsNearby.count() / 8));
    }
}
