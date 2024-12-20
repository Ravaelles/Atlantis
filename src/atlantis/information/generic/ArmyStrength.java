package atlantis.information.generic;

import atlantis.combat.retreating.RetreatManager;
import atlantis.util.cache.Cache;

public class ArmyStrength {

    private static Cache<Boolean> cacheBoolean = new Cache<>();
    private static Cache<Integer> cacheInteger = new Cache<>();

    // =========================================================

    public static boolean weAreWeaker() {
        return ourArmyRelativeStrength() < 100;
    }

    public static boolean weAreStronger() {
        return ourArmyRelativeStrength() >= 108;
//        return ourArmyRelativeStrength() >= 65;
    }

    public static boolean weAreMuchStronger() {
//        System.err.println("ourArmyRelativeStrength() = " + ourArmyRelativeStrength());
        return ourArmyRelativeStrength() >= muchStrongerPercent();
    }

    public static boolean weAreMuchWeaker() {
        return ourArmyRelativeStrength() <= 80;
    }

    private static boolean weAreStronger(int percentAdvantage) {
        return ourArmyRelativeStrength() >= (100 + percentAdvantage);
    }

    public static int ourArmyRelativeStrength() {
        return cacheInteger.get(
            "ourArmyRelativeStrength",
            13,
            () -> (int) Math.min(999, (OurArmy.calculate() * 100 / EnemyArmyStrength.calculate()))
        );
    }

    public static int ourArmyRelativeStrengthWithoutCB() {
        return cacheInteger.get(
            "ourArmyRelativeStrengthWithoutCB",
            13,
            () -> (int) Math.min(999, (OurArmy.calculateWithoutCB() * 100 / EnemyArmyStrength.calculateWithoutCB()))
        );
    }

    // =========================================================

    private static int muchStrongerPercent() {
        int base = 140;

        if (RetreatManager.GLOBAL_RETREAT_COUNTER == 0) {
            return base;
        }
        else if (RetreatManager.GLOBAL_RETREAT_COUNTER <= 2) {
            return base + 10;
        }
        else if (RetreatManager.GLOBAL_RETREAT_COUNTER <= 4) {
            return base + 20;
        }
        else {
            return Math.min(200, base + 30 + RetreatManager.GLOBAL_RETREAT_COUNTER);
        }
    }
}
