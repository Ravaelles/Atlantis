package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.combat.micro.avoid.buildings.protoss.ReaverDontAvoidCB;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.map.base.define.EnemyThirdLocation;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class AvoidCombatBuildingClose extends Manager {
    public static final double MIN_UNSAFE = 12.2;
    public static final double MIN_SAFE = MIN_UNSAFE + 1.8;
    private AUnit combatBuilding;
    private Selection ourCombatUnitsNearby;
    private double dist;
    private Selection friends;

    public AvoidCombatBuildingClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (unit.isRunning()) return false;

        combatBuilding = combatBuilding();
        if (combatBuilding == null) return false;

        dist = unit.distTo(combatBuilding);

//        if (unit.lastUnderAttackLessThanAgo(10)) System.err.println("dist = " + dist);
        if (dist >= 8 && combatBuilding.isBunker()) return false;

        if (asSpecialUnitDontAvoid()) return false;

        friends = combatBuilding.enemiesNear().combatUnits();

//        if (fiercelyEngageEnemyThirdOrExpansions()) return false;
        if (allowBattleDueToAdvantage()) return false;

        if (ifLurkersUndetectedNearbyAvoidTheBuilding()) return true;

        return !strongEnoughToAttack();
    }

    private boolean asSpecialUnitDontAvoid() {
        if (unit.isReaver()) {
            Decision decision = ReaverDontAvoidCB.decision(unit, combatBuilding);
            if (decision.isTrue()) return true;
        }

        return false;
    }


    private boolean allowBattleDueToAdvantage() {
        if (unit.combatEvalRelative() >= 5) return debug("CombatEval5");
        if (A.supplyUsed() >= 190 || unit.friendsNear().combatUnits().atLeast(28)) return debug("HugeBattle");
        if (unit.combatEvalRelative() >= 4 && unit.friendsNear().combatUnits().atLeast(10)) return debug("ManyGuys");

        boolean a = A.supplyUsed() >= 196;
        boolean b = (A.hasMinerals(3000) && A.supplyUsed() >= 180);
        if (a && b) {
            if (
                friends.atLeast(30)
                    || (friends.count() * 16 >= unit.enemiesNear().combatBuildingsAnti(unit).count())
            ) return debug(a ? "MassiveSupply" : "MassiveResources");
        }

//        if (friends.inRadius(10, unit).count() >= atMost(8)) return true;

        return false;
    }

    private boolean debug(String reason) {
//        ErrorLog.printErrorOnce(A.minSec() + " ### ABC ### " + reason);
        unit.addLog(reason);
//        ErrorLog.printErrorOnce(A.minSec() + " ### " + reason + " ###");
        return true;
    }

    @Override
    protected Manager handle() {
        if (shouldRunToMainBecauseWasAttacked()) {
            if (unit.moveToSafety(Actions.MOVE_SAFETY, "CB_A")) return usedManager(this);
        }

//        if (shouldHoldGround(combatBuilding)) {
////            System.err.println("@ " + A.now() + " - HANDLE A - " + unit.typeWithUnitId());
////            unit.holdPosition("HoldHere" + A.dist(dist));
//            unit.holdPosition("CB_Hold" + unit.combatEvalRelativeDigit());
//            return usedManager(this);
//        }

        if (unit.moveAwayFrom(
            combatBuilding, moveAwayDist(), Actions.MOVE_AVOID, "CB_B" + unit.combatEvalRelativeDigit()
        )) return usedManager(this);

        return null;
    }

    private boolean fiercelyEngageEnemyThirdOrExpansions() {
        if (A.supplyUsed() <= 40 || unit.friendsNear().combatUnits().atMost(4)) return false;
        if (unit.combatEvalRelative() <= 0.75) return false;

        APosition enemyThird = EnemyThirdLocation.get();
        if (enemyThird == null) return false;

        if (combatBuilding.friendsNear().combatBuildingsAnti(unit).atLeast(1)) return false;

        return combatBuilding.groundDist(enemyThird) <= 10
            && unit.friendsNear().atLeast(3);

//        APosition enemyNatural = EnemyInfo.enemyNatural();
//        if (enemyNatural == null) return false;
//        if (enemyNatural.groundDistanceTo(unit) <= 18) return false;
//
//        APosition enemyMain = EnemyInfo.enemyMain();
//        if (enemyMain == null) return false;
//        if (enemyMain.groundDistanceTo(unit) <= 18) return false;

//        APosition enemyThird = BaseLocations.enemyThird();
//        if (enemyThird == null) return false;
//        if (unit.distTo(enemyThird) >= 14) return false;
//
//        return true;
    }

    private boolean ifLurkersUndetectedNearbyAvoidTheBuilding() {
        return (A.supplyUsed() <= 150 || OurArmy.strength() <= 160 || Count.observers() <= 0)
            && unit.enemiesNear().lurkers().effUndetected().inRadius(8.4, unit).notEmpty();
//            && unit.friendsNear().detectors().inRadius(6, unit).empty();
    }

    private boolean strongEnoughToAttack() {
        if (A.supplyUsed() <= 70) return false;

//        if ((A.supplyUsed() >= 188 || A.minerals() >= 2000) && unit.squadSize() >= 16) return true;
        if (combatBuilding.enemiesNear().combatUnits().atMost(12)) return false;
        if (ProtossEvaluateAgainstCombatBuildings.chancesLookGood(unit, combatBuilding)) return true;

//        ourCombatUnitsNearby = combatBuilding.enemiesNear().combatUnits();

//        if (unit.combatEvalRelative() >= 2.6) return false;

//        if (dontEngageBecauseTooManyEnemyCombatUnitsNearby()) return true;
//
//        return A.supplyUsed() <= 70 || ourCombatUnitsNearby.atMost(7);

//        if (A.supplyUsed() <= (75 + 7 * unit.enemiesNear().combatBuildingsAntiLand().count())) return false;

        return false;
    }

    private double moveAwayDist() {
        if (unit.enemiesNear().canAttack(unit, 3.2 + (unit.isMelee() ? 2 : 0)).notEmpty()) return 3;

        if (dist <= 6) return 3;
        if (dist <= 7) return 2.6;
        if (dist <= 8) return 2.2;
        return 1.6;

//        return unit.hpPercent(40) ? 0.1 : 0.4;
    }

    private double minUnsafe() {
        return MIN_UNSAFE + unit.squadSize() / 6.0 + (unit.isMelee() ? 5 : 0);
    }

//    private boolean shouldHoldGround(AUnit combatBuilding) {
//        if (unit.lastUnderAttackLessThanAgo(90)) return false;
//        if (unit.lastUnderAttackLessThanAgo(40)) return false;
//
//        if (
//            dist >= MIN_SAFE && dist <= MIN_SAFE + 0.5
//                && unit.friendsNear().atLeast(10)
//                && unit.friendsInRadiusCount(1) >= 1
//        ) {
//            return true;
//        }
//
//        if (dist <= minUnsafe()) return false;
//        if (unit.enemiesNear().nonBuildings().canAttack(unit, 1.3).notEmpty()) return false;
//
////        double minDistAllowed = (unit.isRanged() ? MIN_SAFE : (MIN_SAFE + 1.5)) ;
//
////        if (
////            dist <= minDistAllowed && (
////                !unit.isAttacking() || !unit.isTargetInWeaponRangeAccordingToGame()
////            )
////        ) return true;
//
//        return dist >= (MIN_SAFE + (unit.woundPercent() / 40.0));
////        return unit.isMoving()
////            && unit.targetPosition() != null
////            && dist < minDistAllowed;
//    }

    // =========================================================


    private AUnit combatBuilding() {
        return unit.enemiesNear()
            .buildings()
            .onlyCompleted()
            .combatBuildingsAnti(unit)
            .inGroundRadius(15, unit)
            .nearestTo(unit);
    }

//    private boolean combatBuildingShouldNotBeEngaged() {
//        ourCombatUnitsNearby = combatBuilding.enemiesNear().combatUnits();
//
////        if (unit.combatEvalRelative() >= 2.6) return false;
//
//        if (dontEngageBecauseTooManyEnemyCombatUnitsNearby()) return true;
//
//        return A.supplyUsed() <= 70 || ourCombatUnitsNearby.atMost(7);
//    }

//    private boolean dontEngageBecauseTooManyEnemyCombatUnitsNearby() {
//        if (A.supplyUsed() >= 170 || A.hasMinerals(3000)) return false;
//        if (unit.friendsNear().combatUnits().atLeast(25)) return false;
//
//        return combatBuilding.enemiesNear()
//            .combatUnits()
//            .inRadius(6, combatBuilding).atMost((int) (ourCombatUnitsNearby.count() / 8));
//    }

    private boolean shouldRunToMainBecauseWasAttacked() {
        if (dist >= 7.9 || unit.lastUnderAttackLessThanAgo(50)) return true;

        return false;
    }
}
