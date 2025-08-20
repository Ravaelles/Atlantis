package atlantis.combat.micro.avoid.dont.protoss.dragoon;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class DragoonDontAvoidVsProtoss {
    static boolean vsProtoss(AUnit unit) {
//        if (true) return false;

        if (dontAvoidWhenOnlyEnemyZealotsNearby(unit)) return true;
//        if (dontAvoidZealotsSoMuchWhenEnemyGoonsNearby(unit)) return true;
        if (dontAvoidBigGoonBattle(unit)) return true;

//        System.out.println(unit.lastUnderAttackAgo());
//        if (unit.shieldWound() <= 10 && unit.lastUnderAttackMoreThanAgo(30 * 8)) return true;

//        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(3.7);
//        if (meleeEnemiesNearCount > 0) {
////            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - MELEE NEAR");
//            return false;
////            return whenMeleeNear(unit);
//        }


        Decision decision;
        if ((decision = oneOnOneDragoon(unit)).notIndifferent()) return decision.toBoolean();

        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        return false;
//
////        if (unit.hp() <= 41 && !unit.isSafeFromMelee()) return false;
//        if (!unit.isSafeFromMelee()) return false;
//
//        return (unit.woundHp() <= 30 || unit.combatEvalRelative() > 1.05)
//            && reason(unit, "VsProtoss");
    }

    private static boolean dontAvoidBigGoonBattle(AUnit unit) {
        if (unit.enemiesNear().ranged().empty()) return false;
        if (unit.meleeEnemiesNearCount(3) > 0) return false;

        int cooldown = unit.cooldown();

//        if (unit.hp() >= 80) return true;
        if (unit.shieldWound() <= 8) return true;
        if (cooldown <= 8) return true;
        if (!unit.shotSecondsAgo(2)) return true;
        if (A.isUms() && cooldown <= 10) return true;
        if (unit.hp() >= 80 && cooldown <= 16) return true;
        if (unit.hp() >= 60 && cooldown <= 10) return true;
//        if (cooldown <= 7 && unit.eval() >= 0.8) return true;
        if (cooldown <= 14 && unit.shields() >= 5) return true;

        if (cooldown <= 12 && unit.eval() <= 1.3) {
            if (
                (1 + unit.friendsNear().dragoons().countInRadius(6, unit))
                    >= unit.enemiesNear().dragoons().countInRadius(6, unit) + 2
            ) return true;
        }

        return unit.eval() >= 0.8 && unit.hp() >= 18 && unit.enemiesNear().dragoons().atLeast(4);
    }

    private static boolean dontAvoidZealotsSoMuchWhenEnemyGoonsNearby(AUnit unit) {
        return false;
    }

    private static boolean dontAvoidWhenOnlyEnemyZealotsNearby(AUnit unit) {
        if (!Enemy.protoss()) return false;
        if (unit.hp() <= 33) return false;

        Selection enemiesNear = unit.enemiesNear().havingAntiGroundWeapon().inRadius(10, unit);

        if (!enemiesNear.onlyMelee()) return false;

        if (unit.shieldWound() <= 23 && enemiesNear.inRadius(2.8, unit).empty()) return true;

//        if (unit.shieldWounded() && enemiesNear.inRadius(2.5, unit).notEmpty()) return false;
//        if (enemiesNear.ranged().canAttack(unit, 0.5).notEmpty()) return false;

        if (unit.shieldWound() >= 23 && enemiesNear.inRadius(2.5, unit).atLeast(2)) return false;

        if (
            unit.enemiesNear().ranged().canAttack(unit, 0.4 + unit.woundPercent() / 60.0).empty()
                && enemiesNear.melee().inRadius(3.2, unit).empty()
        ) {
//            unit.paintCircleFilled(14, Color.Green);
            return DragoonDontAvoid.dontAvoid(unit, "OnlyMeleeA");
        }

        return unit.shieldDamageAtMost(29)
            && unit.meleeEnemiesNearCount(2.8) <= 0
            && DragoonDontAvoid.dontAvoid(unit, "OnlyMeleeB");
    }

    private static Decision oneOnOneDragoon(AUnit unit) {
        Selection enemies = unit.enemiesNear().groundUnits().combatUnits().canAttack(unit, 3.1);
        if (enemies.size() >= 2) return Decision.INDIFFERENT;
        if (!enemies.onlyRanged()) return Decision.INDIFFERENT;

//        if (unit.cooldown() >= 11) return Decision.FALSE;

//        if (unit.cooldown() <= 8) {
//            System.out.println("A");
//            DragoonDontAvoid.reason(unit, "ToraToraTora");
//            return Decision.TRUE;
//        }

        AUnit enemy = enemies.first();
//        if (enemy != null && enemies.onlyOfType(AUnitType.Protoss_Dragoon)) {
        if (enemy != null && enemies.onlyOfType(AUnitType.Protoss_Dragoon)) {
//            System.out.println("B");
            if (unit.hp() >= enemy.hp() - 22) {
//                System.out.println("ToraTora");
                DragoonDontAvoid.dontAvoid(unit, "1v1Dragoon");
                return Decision.TRUE;
            }
        }

        return Decision.INDIFFERENT;
    }
}
