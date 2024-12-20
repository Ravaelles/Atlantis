package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.units.AUnit;

public class ZealotSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public ZealotSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (
            defender.hp() <= (defender.meleeEnemiesNearCount(1.6) <= 1 ? 23 : 36)
                && defender.friendsNear().combatUnits().havingAntiGroundWeapon().countInRadius(3, defender) == 0
        ) return 3.8;

        if (defender.hp() <= 20) return baseValueAgainst(attacker)
            + 2;

        return -1;

//        double margin = baseValueAgainst(attacker)
//            + (defender.woundPercent() / 62.0)
//            + (defender.cooldown() >= 14 ? +0.4 : 0)
//            + (defender.cooldown() >= 6 ? +0.4 : 0);
//
//        margin = Math.min(OurDragoonRange.range() - 0.3, margin);
//
////        System.err.println("@" + A.now + " safetyMargin = " + margin + " " + defender.digitDistTo(attacker));
////        defender.paintCircle((int) (margin * 32), Red);
////        defender.paintCircle((int) (margin * 32) + 1, Red);
//
//        return margin;

    }

    private double baseValueAgainst(AUnit attacker) {
        boolean lookingAtUs = attacker.isTarget(defender) || defender.isOtherUnitFacingThisUnit(attacker);

        if (!lookingAtUs) return 2.0;

//        if (attacker.isZealot()) return baseVsZealot();
//        if (attacker.isZergling()) return baseVsZergling();

        return 2.3;
    }

//    private double baseVsZealot() {
//        return 2.3
//            + (defender.meleeEnemiesNearCount(3) >= 2 ? 0.4 : 0)
//            + (defender.meleeEnemiesNearCount(3.5) >= 3 ? 0.4 : 0)
//            + (defender.meleeEnemiesNearCount(3.5) >= 4 ? 0.4 : 0)
//            + (defender.meleeEnemiesNearCount(3.8) >= 3 ? 0.4 : 0)
//            + (defender.meleeEnemiesNearCount(4.5) >= 5 ? 0.4 : 0);
//    }
//
//    private double baseVsZergling() {
//        double base = 2.08;
//        int meleeEnemiesNear = defender.meleeEnemiesNearCount(3.5);
//
//        if (defender.cooldown() >= 4) {
//            base += (meleeEnemiesNear >= 3 ? 0.4 : 0)
//                + (meleeEnemiesNear >= 4 ? 0.4 : 0);
//        }
//
//        return base
//            + (defender.cooldown() >= 10 ? 2.0 : 0)
//            + (
//            defender.shotSecondsAgo() >= 1.5
//                && meleeEnemiesNear <= (defender.shields() >= 10 ? 2 : 1)
//                ? -1.5 : 0
//        );
//    }
}
