package atlantis.information.generic;

import atlantis.util.cache.Cache;

public class ArmyStrength {

    private static final Cache<Boolean> cacheBoolean = new Cache<>();
    private static final Cache<Integer> cacheInteger = new Cache<>();

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
            () -> (int) Math.min(999, (Army.calculate() * 100 / EnemyArmyStrength.calculate()))
        );
    }

    public static int ourArmyRelativeStrengthWithoutCB() {
        return cacheInteger.get(
            "ourArmyRelativeStrengthWithoutCB",
            13,
            () -> (int) Math.min(999, (Army.calculateWithoutCB() * 100 / EnemyArmyStrength.calculateWithoutCB()))
        );
    }

    public static int ourArmyRelativeStrengthWithoutOurCB() {
        return cacheInteger.get(
            "ourArmyRelativeStrengthWithoutOurCB",
            13,
            () -> (int) Math.min(999, (Army.calculateWithoutCB() * 100 / EnemyArmyStrength.calculate()))
        );
    }

    // =========================================================

    public static void clearCache() {
        cacheBoolean.clear();
        cacheInteger.clear();
    }

    /**
     * The bar for "much stronger". The retreat counter that used to lift
     * it died with RetreatManager (never written, constant 0) - the bar
     * is the 140 it always was. Rewiring the lift is new behavior, not
     * part of this deletion.
     */
    private static int muchStrongerPercent() {
        return 140;
    }
}
