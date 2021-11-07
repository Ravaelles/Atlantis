package atlantis.strategy.decisions;

import atlantis.enemy.EnemyInformation;
import atlantis.strategy.OurStrategy;
import atlantis.util.Cache;

public class OurDecisions {

    private static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    public static boolean haveFactories() {
        return cache.get(
            "haveFactories",
            100,
            () -> {
                return OurStrategy.get().goingBio();
            }
        );
    }

    public static boolean beAbleToProduceTanks() {
        return cache.get(
            "beAbleToProduceTanks",
            100,
            () -> {
                return OurStrategy.get().goingBio();
            }
        );
    }

    public static boolean shouldBuildBio() {
        return cache.get(
                "buildBio",
                100,
                () -> !EnemyInformation.enemyStartedWithDefensiveBuilding && OurStrategy.get().goingBio()
        );
    }
}
