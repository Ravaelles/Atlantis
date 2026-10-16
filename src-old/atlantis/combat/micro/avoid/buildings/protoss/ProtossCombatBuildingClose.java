package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.combat.micro.avoid.buildings.AllowAvoidingCB;
import atlantis.combat.micro.avoid.buildings.DontAvoidBunker;
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
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossCombatBuildingClose extends Manager {
    public static final double MIN_UNSAFE = 12.2;
    public static final double MIN_SAFE = MIN_UNSAFE + 1.8;
    private AUnit combatBuilding;
    private Selection ourCombatUnitsNearby;
    private double dist;
    private Selection friends;

    public ProtossCombatBuildingClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (ignoreByUnitType()) return false;

        Decision decision;
//        if (unit.isRunning()) return false;

        combatBuilding = combatBuilding();
        if (combatBuilding == null) return false;

        AUnit leader = unit.leader();
        if (leader == null) return t("No leader");

        // === Exception - skip ====================================

        if (AllowAvoidingCB.allowed(unit, combatBuilding, leader)) return false;

        // =========================================================

        if (leader.lastActionLessThanAgo(30 * 17, Actions.MOVE_AVOID)) return t("LeaderAvoidCB");

        if (Enemy.zerg() && Army.strength() <= 400 && A.supplyUsed() <= 160) return t("YesVsZerg");

        if ((unit.isRunning() || unit.isRetreating()) && combatBuilding.distTo(unit) >= 18) return f("Running");
        if ((decision = forReaver()).notIndifferent()) return decision.toBoolean();

        if (!unit.squadIsAlpha() && unit.eval() >= 1.2 && unit.hasGroundWeapon()) return f("Non-alpha, dont ACB");

        if (unit.meleeEnemiesNearCount(1.4) > 0) return f("melee enemies");
        if (dontAttackDueToRangedEnemiesNear()) return f("ranged enemies");

        if (unit.isProtoss()) {
            decision = ShouldAvoidCombatBuildingAsProtoss.decision(unit, combatBuilding);
            if (decision.notIndifferent()) return trOrF(decision.toBoolean(), decision.reason());
        }

        else if (We.terran()) {
            if (
                unit.eval() >= 1.8
                    && unit.friendsNearCount() >= 6
                    && unit.lastStartedRunningMoreThanAgo(30 * 8)
            ) return f("Strong terran");
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

    private boolean ignoreByUnitType() {
        if (We.protoss()) {
            if (unit.isAir() && unit.isCorsair()) return true;
            if (unit.isDarkTemplar()) return true;
        }

        return false;
    }

    @Override
    protected Manager handle() {
//        if (ignoreOrderBecauseWeDontHaveToRetreat()) return null;
//        if (combatBuilding == null || !combatBuilding.hasValidTarget()) return null;
        if (combatBuilding == null) return null;
        if (unit.distTo(combatBuilding) >= 18) return null;

//        System.err.println("combatBuilding = " + combatBuilding);

        if (unit.moveToSafety(Actions.MOVE_AVOID, "CB_A")) return usedManager(this);

        if (unit.moveAwayFrom(
            combatBuilding, moveAwayDist(), Actions.MOVE_AVOID
        )) return usedManager(this, "CB_B" + unit.evalDigit());

//        if (shouldRunToMainBecauseWasAttacked()) {
//        }

        if (unit.runningManager().runFrom(combatBuilding, 3, Actions.MOVE_AVOID, true)) {
            return usedManager(this, "CB_A2");
        }

//        if (shouldHoldGround(combatBuilding)) {
////            System.err.println("@ " + A.now() + " - HANDLE A - " + unit.typeWithUnitId());
////            unit.holdPosition("HoldHere" + A.dist(dist));
//            unit.holdPosition("CB_Hold" + unit.combatEvalRelativeDigit());
//            return usedManager(this);
//        }

        return null;
    }

    private Decision forReaver() {
        if (!unit.isReaver()) return Decision.INDIFFERENT;

        if (unit.shieldHealthy()) return Decision.FALSE;
        if (unit.cooldown() > 0) return Decision.FALSE;
        if (unit.lastActionLessThanAgo(30, Actions.UNLOAD)) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }

    private boolean dontAttackDueToRangedEnemiesNear() {
        int minRangedEnemies = unit.hp() >= 81 && unit.eval() >= 2.5 ? 3 : 1;

        return unit.enemiesNear().ranged().nonBuildings().canAttack(unit, 1.7).count() >= minRangedEnemies;
    }

    public static boolean t(String reason) {
//        System.out.println("AllowAvoidingCB: " + reason);
//        if (true) throw new RuntimeException("wut");

        return true;
    }

    public static boolean trOrF(boolean result, String reason) {
        return result ? t(reason) : f(reason);
    }

    public static boolean f(String reason) {
//        System.out.println("Don't AllowAvoidingCB: " + reason);

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

//    private boolean ignoreOrderBecauseWeDontHaveToRetreat() {
//        if ((unit.isReaver() || unit.isTank()) && unit.woundPercent() >= 25) return false;
//        if (unit.eval() <= 1.4) return false;
//        if (unit.hp() <= 42 && We.protoss() && unit.eval() <= 2.5) return false;
//
//        return !(new RetreatManager(unit)).invokedFrom(this);
//    }

    private boolean fiercelyEngageEnemyThirdOrExpansions() {
        if (A.supplyUsed() <= 40 || unit.friendsNear().combatUnits().atMost(4)) return false;
        if (unit.eval() <= 1.5) return false;

        APosition enemyNatural = EnemyNaturalBase.get();
        if (enemyNatural == null) return false;

        APosition enemyThird = EnemyThirdBase.position();
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

        if (ProtossAvoidCombatBuildingKeepFar.shouldKeepFar(unit)) return ProtossAvoidCombatBuildingKeepFar.DIST(unit);

//        if (unit.isReaver()) return unit.shields() >= 40 ? 7.2 : 8.7;
        if (unit.isReaver()) {
            if (!unit.shotSecondsAgo(8) && unit.hp() >= 60) return 5.0;
            return unit.shields() >= 40 ? 6.6 : 7.7;
        }

        return 11.4
            + (400.0 / Army.strength())
            + (unit.isMelee() ? 2 : 0)
            + (unit.woundPercent() / 25.0)
            + (unit.woundPercent() >= 60 ? 1.0 : 0)
            + (unit.woundPercent() >= 80 ? 0.5 : 0)
            + ((A.s % 10) / 5.0);
    }

    public AUnit combatBuilding() {
        double radius = radius();

        return Select.enemyCombatUnits()
            .buildings()
            .onlyCompleted()
            .combatBuildingsAnti(unit)
            .inRadius(radius, unit)
            .notUnpowered()
            .canAttack(unit, 5)
            .nearestTo(unit);
    }
}
