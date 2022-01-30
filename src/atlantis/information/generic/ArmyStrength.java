package atlantis.information.generic;

import atlantis.util.Cache;

public class ArmyStrength {

    private static Cache<Boolean> cacheBoolean = new Cache<>();

    // =========================================================

    public static boolean weAreStronger() {
        return weAreStronger(15);
    }

    public static boolean weAreStronger(int percentAdvantage) {
        return cacheBoolean.get(
                "weAreStronger:" + percentAdvantage,
                50,
                () -> (OurArmyStrength.calculate() * (100 + percentAdvantage) / 100) > EnemyArmyStrength.calculate()
        );
    }

}
