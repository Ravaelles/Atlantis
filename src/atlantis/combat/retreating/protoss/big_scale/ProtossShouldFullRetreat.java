package atlantis.combat.retreating.protoss.big_scale;

import atlantis.combat.micro.avoid.dont.protoss.DontAvoidWhenCannonsNear;
import atlantis.combat.squad.Squad;
import atlantis.config.env.Env;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ProtossShouldFullRetreat {
    private static AUnit unit;
    private static double eval;

    private static boolean f(String reasonWhyNot) {
//        System.err.println("    ProtossShouldFullRetreat NO because: " + reasonWhyNot);
        return false;
    }

    public static boolean shouldFullRetreat(AUnit unit) {
        ProtossShouldFullRetreat.unit = unit;

        if (unit.isRunning()) return f("Unit running");

        Selection enemies = unit.enemiesNear().combatUnits().inRadius(Enemy.terran() ? 14 : 10, unit);
        if (enemies.empty()) return f("Enemy empty");

        AUnit leader = unit.squadLeader();
        if (leader != null && !unit.isLeader()) {
//            if (leader.isRetreating()) return true;
            return leader.isRetreating();
        }

        eval = unit.eval();

        Decision decision;
        if ((decision = ProtossRetreatFromSunken.decision(unit)).notIndifferent()) return decision.toBoolean();

        if (eval >= 10 && Count.ourCombatUnits() >= 10) return f("Very high eval");

        if (retreatDuringMissionAttack(unit)) return true;

        if (
            eval >= 0.4 && (Army.strength() >= 600 && A.supplyUsed() >= 60) || A.minerals() >= 2000
        ) return f("Very wealthy");

        if (DontAvoidWhenCannonsNear.check(unit)) return f("Cannons near");
        if (combatEvalIsTooHighToRetreat()) return f("Eval too high to retreat");
        if (dontRunNearOurCombatBuildings()) return f("Near our combat buildings");

        if (eval <= 0.72 && (leader == null || (!leader.equals(unit) && leader.eval() <= 0.75))) return true;

        if (A.isUms() && !Env.isTesting() && Count.bases() == 0) return f("UMS no bases");

        if (!unit.isMissionAttack() && eval >= 0.7) return f("Not mission attack and eval high");
//        if (Enemy.protoss() && unit.eval() >= 0.91) return f("");
        if (unit.distToBase() <= 4) return f("Too close to base");
//        if (unit.eval() >= 0.75 && unit.ourNearestBuildingDist() <= 3) return f("");

        if (ProtossApprxRetreat.check(unit)) return true;

        if (A.s <= 600 && (Enemy.protoss() || Enemy.zerg()) && EnemyUnits.discovered().ranged().empty()) {
            if (enemies.canAttack(unit, 2.8 + unit.woundPercent() / 100.0).empty()) return f("No ranged enemies can attack");
        }

        if (unit.friendsNear().inRadius(2, unit).atLeast(6)) return f("Many friends in radius 2");
        if (unit.friendsNear().inRadius(4, unit).atLeast(10)) return f("Many friends in radius 4");

        if (Enemy.zerg() && unit.isMelee() && unit.shields() >= 30 && unit.meleeEnemiesNearCount(1.3) == 1) {
            return f("Zerg melee, high shields, 1 enemy");
        }

//        AChoke naturalChoke = Chokes.natural();
////        APosition naturalBase = DefineNaturalBase.natural();
//        AUnit naturalBase = Bases.natural();
//        if (
//            naturalChoke != null
//                && naturalBase != null
//                && naturalChoke.distTo(unit) >= 2.5
////                && naturalChoke.distTo(unit) <= 8
//                && naturalBase.distTo(unit) <= 8
//        ) return f("");

        if (eval <= 0.92) return f("Eval too low");

        return f("GenericNo");
    }

    private static boolean retreatDuringMissionAttack(AUnit unit) {
        if (!unit.isMissionAttack()) return false;

        double base = A.s <= 60 * 7 ? 1.2 : 1.15;
        if (Enemy.zerg()) base += 0.1;

        return eval <= base;
    }

    private static boolean dontRunNearOurCombatBuildings() {
        if (Count.cannons() == 0) return false;

        return unit.friendsNear().cannons().countInRadius(3.2, unit) >= 1;
    }

    private static boolean combatEvalIsTooHighToRetreat() {
        Squad squad = unit.squad();
        if (squad != null && squad.cohesionPercent() <= 60) return false;

        double threshold = unit.isMissionDefendOrSparta() ? 0.95 : 1.12;

        threshold -= unit.squadSize() / 100.0; // 25 units => extra -0.25 allowed

        if (A.supplyUsed() <= 80) threshold += 0.1;

        return unit.eval() >= threshold;
    }

//    private double applyTweaksToCombatEval() {
//        return unit.combatEvalRelative()
//            + (unit.isMissionDefendOrSparta() ? 0 : (unit.distToNearestChokeLessThan(4) ? -0.4 : 0))
//            + (unit.lastRetreatedAgo() <= 30 * 4 ? -0.25 : 0)
////            + (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
//            + (unit.distToMain() <= 20 ? +0.15 : 0)
//            + (unit.lastUnderAttackLessThanAgo(30 * 4) ? -0.05 : 0)
////            + combatBuildingPenalty(unit)
//            + enemyZerglingBonus(unit);
//    }
}
