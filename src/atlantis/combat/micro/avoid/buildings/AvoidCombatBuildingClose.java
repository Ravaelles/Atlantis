package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.combat.micro.avoid.buildings.protoss.ReaverDontAvoidCB;
import atlantis.combat.retreating.RetreatManager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.map.base.define.EnemyNaturalBase;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.We;

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

        if (We.protoss() && combatBuilding.isBunker()) {
            Decision decision;
            if ((decision = ShouldAvoidBunkerAsProtoss.decision(unit, combatBuilding)).notIndifferent())
                return decision.toBoolean();
        }

        dist = unit.distTo(combatBuilding);

//        if (unit.lastUnderAttackLessThanAgo(10)) System.err.println("dist = " + dist);
        if (DontAvoidBunker.dontAvoid(unit, combatBuilding, dist)) return false;
        if (asSpecialUnitDontAvoid()) return false;

        friends = combatBuilding.enemiesNear().combatUnits();

//        if (unit.woundPercent() <= 5 && unit.enemiesNear().inRadius(7.5, unit).notEmpty()) return false;

        if (
            unit.friendsNear().combatUnits().countInRadius(6, unit) <= 6 || moreEnemiesThanOur()
        ) return true;

        if (fiercelyEngageEnemyThirdOrExpansions()) return false;
        if (allowBattleDueToAdvantage()) return false;

        if (ifLurkersUndetectedNearbyAvoidTheBuilding()) return true;

        return !strongEnoughToAttack();
    }

    private boolean moreEnemiesThanOur() {
        double enemyRaceModifier = Enemy.zerg() ? 0.7 : (Enemy.protoss() ? 1.0 : 0.8);
        Selection our = unit.friendsNear().combatUnits();
        int bonus = (int) (our.dragoons().count() / 3.6);

        return our.countInRadius(6, unit) + 1 + bonus
            <= enemyRaceModifier * unit.enemiesNear().combatUnits().count();
    }

    private boolean asSpecialUnitDontAvoid() {
        if (unit.isReaver()) {
            Decision decision = ReaverDontAvoidCB.decision(unit, combatBuilding);
            if (decision.notIndifferent()) return decision.toBoolean();
        }

        if (unit.isAir() && unit.type().isDetectorNonBuilding() && dist <= (7.5 + unit.woundPercent() / 30.0)) {
            unit.setTooltip("DetectorCloseCB");
            return true;
        }

        return false;
    }


    private boolean allowBattleDueToAdvantage() {
        if (unit.eval() < 3.5) return false;
        if (unit.eval() >= 6) return allowBattle("CombatEval5");
        if (A.supplyUsed() >= 190 || unit.friendsNear().combatUnits().atLeast(28)) return allowBattle("HugeBattle");
        if (unit.eval() >= 4 && unit.friendsNear().combatUnits().atLeast(10))
            return allowBattle("ManyGuys");

        int ourCombat = unit.friendsNear().combatUnits().count();
        int enemyCombat = unit.enemiesNear().combatUnits().count();
        if (
            ourCombat >= 7 * enemyCombat
                && combatBuilding.friendsNear().combatBuildings(false).empty()
        ) return allowBattle("OnlyOneCB");

        boolean a = A.supplyUsed() >= 196;
        boolean b = (A.hasMinerals(1500) && A.supplyUsed() >= 170);
        if (a && b) {
            if (
                friends.atLeast(30)
                    || (friends.count() * 16 >= unit.enemiesNear().combatBuildingsAnti(unit).count())
            ) return allowBattle(a ? "MassiveSupply" : "MassiveResources");
        }

//        if (friends.inRadius(10, unit).count() >= atMost(8)) return true;

        return false;
    }

    private boolean allowBattle(String reason) {
//        ErrorLog.printErrorOnce(A.minSec() + " ### ABC ### " + reason);
        unit.addLog(reason);
//        ErrorLog.printErrorOnce(A.minSec() + " ### " + reason + " ###");
        return true;
    }

    @Override
    protected Manager handle() {
//        if (ignoreOrderBecauseWeDontHaveToRetreat()) return null;

        if (unit.moveAwayFrom(
            combatBuilding, moveAwayDist(), Actions.MOVE_AVOID
        )) return usedManager(this, "CB_B" + unit.evalDigit());

//        if (shouldRunToMainBecauseWasAttacked()) {
//            if (unit.moveToSafety(Actions.MOVE_SAFETY, "CB_A")) return usedManager(this);
//        }

        if (unit.runningManager().runFrom(combatBuilding, 3, Actions.MOVE_SAFETY, true)) {
            return usedManager(this, "CB_A");
        }

//        if (shouldHoldGround(combatBuilding)) {
////            System.err.println("@ " + A.now() + " - HANDLE A - " + unit.typeWithUnitId());
////            unit.holdPosition("HoldHere" + A.dist(dist));
//            unit.holdPosition("CB_Hold" + unit.combatEvalRelativeDigit());
//            return usedManager(this);
//        }

        return null;
    }

    private boolean ignoreOrderBecauseWeDontHaveToRetreat() {
        if ((unit.isReaver() || unit.isTank()) && unit.woundPercent() >= 25) return false;
        if (unit.eval() <= 1.4) return false;
        if (unit.hp() <= 42 && We.protoss() && unit.eval() <= 2.5) return false;

        return !(new RetreatManager(unit)).invokedFrom(this);
    }

    private boolean fiercelyEngageEnemyThirdOrExpansions() {
        if (A.supplyUsed() <= 40 || unit.friendsNear().combatUnits().atMost(4)) return false;
        if (unit.eval() <= 1.5) return false;

        APosition enemyNatural = EnemyNaturalBase.get();
        if (enemyNatural == null) return false;

        APosition enemyThird = EnemyThirdBase.get();
        if (enemyThird == null) return false;

        if (combatBuilding.friendsNear().combatBuildingsAnti(unit).atLeast(A.supplyUsed() <= 140 ? 1 : 2)) return false;

        return combatBuilding.groundDist(enemyThird) <= 10
            && unit.friendsNear().atLeast(3)
            && combatBuilding.friendsNear().combatUnits().countInRadius(10, combatBuilding) <= 5;

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
        return (A.supplyUsed() <= 150 || Army.strength() <= 180 || Count.observers() <= 0)
            && unit.enemiesNear().lurkers().burrowed().effUndetected().inRadius(8.4, unit).notEmpty();
//            && unit.friendsNear().detectors().inRadius(6, unit).empty();
    }

    private boolean strongEnoughToAttack() {
        if (A.supplyUsed() <= 70) return false;

        if (Army.strength() >= 800 && unit.eval() >= 1.3) return true;

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
            .inRadius(8.2, unit)
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
