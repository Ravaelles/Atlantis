package atlantis.information.generic;

import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.util.Cache;

public class ArmyStrength {

    private static Cache<Boolean> cacheBoolean = new Cache<>();
    private static Cache<Integer> cacheInteger = new Cache<>();

    // =========================================================

    public static boolean weAreWeaker() {
        return ourArmyRelativeStrength() < 120;
    }

    public static boolean weAreStronger() {
        return ourArmyRelativeStrength() >= 120;
    }

    public static boolean weAreMuchStronger() {
        return ourArmyRelativeStrength() >= muchStrongerPercent();
    }

    public static boolean weAreMuchWeaker() {
        return ourArmyRelativeStrength() <= 80;
    }

    private static boolean weAreStronger(int percentAdvantage) {
        return cacheBoolean.get(
                "weAreStronger:" + percentAdvantage,
                50,
                () -> (OurArmyStrength.calculate() * (100 - percentAdvantage) / 100) > EnemyArmyStrength.calculate()
        );
    }

    public static int ourArmyRelativeStrength() {
        return cacheInteger.get(
                "ourArmyRelativeStrength",
                50,
                () -> Math.min(999, (int) (OurArmyStrength.calculate() * 100 / EnemyArmyStrength.calculate()))
        );
    }

    // =========================================================

    private static int muchStrongerPercent() {
        int base = 40;

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
            return base + 30 + RetreatManager.GLOBAL_RETREAT_COUNTER;
        }
    }
}
