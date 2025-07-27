package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.combat.micro.avoid.buildings.protoss.ShouldAvoidCombatBuildingAsProtoss;
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

        if ((unit.isRunning() || unit.isRetreating()) && combatBuilding.distTo(unit) >= 18) return false;

        if (unit.meleeEnemiesNearCount(1.4) > 0) return f("melee enemies");
        if (unit.enemiesNear().ranged().nonBuildings().canAttack(unit, 1.1).count() > 0) return f("ranged enemies");

        if (We.terran()) {
            if (
                unit.eval() >= 1.8
                    && unit.friendsNearCount() >= 6
                    && unit.lastStartedRunningMoreThanAgo(30 * 8)
            ) return f("Strong terran");
        }

        if (We.protoss()) {
            Decision decision = ShouldAvoidCombatBuildingAsProtoss.decision(unit, combatBuilding);
            if (decision.notIndifferent()) return trOrF(decision.toBoolean(), decision.reason());
        }

        dist = unit.distTo(combatBuilding);

//        if (unit.lastUnderAttackLessThanAgo(10)) System.err.println("dist = " + dist);
        if (DontAvoidBunker.dontAvoid(unit, combatBuilding, dist)) return f("Dont avoid bunker");
        if (asSpecialUnitDontAvoid()) return f("Special unit don't avoid");

        friends = combatBuilding.enemiesNear().combatUnits();

//        if (unit.woundPercent() <= 5 && unit.enemiesNear().inRadius(7.5, unit).notEmpty()) return false;

        if (
            unit.friendsNear().combatUnits().countInRadius(6, unit) <= 6 || moreEnemiesThanOur()
        ) return t("not strong enough to attack");

        if (fiercelyEngageEnemyThirdOrExpansions()) return f("Fiercely engage enemy third or expansion");
        if (allowBattleDueToAdvantage()) return f("Allow battle due to advantage");

        if (ifLurkersUndetectedNearbyAvoidTheBuilding()) return t("Lurkers undetected nearby");

        return !strongEnoughToAttack() ? t("not strongEnoughToAttack") : f("strongEnoughToAttack");
    }

    private boolean t(String reason) {
//        System.out.println("AvoidCB: " + reason);

        return true;
    }

    private boolean trOrF(boolean result, String reason) {
        return result ? t(reason) : f(reason);
    }

    private boolean f(String reason) {
//        System.out.println("Don't AvoidCB: " + reason);

        return false;
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
//        if (combatBuilding == null || !combatBuilding.hasValidTarget()) return null;
        if (combatBuilding == null) return null;

        if (unit.moveToSafety(Actions.MOVE_AVOID, "CB_A")) return usedManager(this);

        if (unit.moveAwayFrom(
            combatBuilding, moveAwayDist(), Actions.MOVE_AVOID
        )) return usedManager(this, "CB_B" + unit.evalDigit());

//        if (shouldRunToMainBecauseWasAttacked()) {
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

//    private double minUnsafe() {
//        return MIN_UNSAFE + unit.squadSize() / 6.0 + (unit.isMelee() ? 5 : 0);
//    }

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

    /// /        return unit.isMoving()
    /// /            && unit.targetPosition() != null
    /// /            && dist < minDistAllowed;
//    }

    // =========================================================
    private double baseDist() {
        if (AvoidCombatBuildingKeepFar.shouldKeepFar()) return AvoidCombatBuildingKeepFar.DIST(unit);

        if (unit.isReaver()) return unit.shields() >= 40 ? 8.2 : 8.7;

        if (Enemy.zerg()) return 9.7 + (400.0 / Army.strength());

        return 9.7;
    }

    private double radius() {
//        if ((Enemy.zerg() || Enemy.protoss()) && We.protoss()) {
//            if (
//                unit.isRanged()
//                    && (unit.shieldWound() <= 14 || unit.lastUnderAttackMoreThanAgo(30 * 25))
//                    && unit.shieldWound() <= 30
//                    && unit.friendsNear().dragoons().atLeast(4)
//            ) {
//                double radius = 8.7;
//
//                if (unit.isDragoon()) {
//                    radius = 9.0 + (unit.woundPercent() / 60.0);
//                }
//
//                return radius;
//            }
//        }

        return baseDist()
            + (unit.isMelee() ? 1.7 : 0)
            + (unit.isWounded() ? 0.8 : 0)
            + (unit.woundPercent() >= 60 ? 1.0 : 0)
            + (unit.woundPercent() >= 80 ? 0.5 : 0)
            + ((A.s % 10) / 5.0);
    }

    private AUnit combatBuilding() {
        double radius = radius();

        return unit.enemiesNear()
            .buildings()
            .onlyCompleted()
            .combatBuildingsAnti(unit)
            .inRadius(radius, unit)
            .nearestTo(unit);
    }

//    private boolean combatBuildingShouldNotBeEngaged() {
//        ourCombatUnitsNearby = combatBuilding.enemiesNear().combatUnits();
//

    /// /        if (unit.combatEvalRelative() >= 2.6) return false;
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
//    private boolean shouldRunToMainBecauseWasAttacked() {
//        if (dist >= 7.9 || unit.lastUnderAttackLessThanAgo(50)) return true;
//
//        return false;
//    }
}
