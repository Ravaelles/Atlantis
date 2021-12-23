package atlantis.strategy;

public class EnemyStrategy {
    
    private static AStrategy enemyStrategy = null;
    
    // =========================================================

    public static boolean isEnemyStrategyKnown() {
        return !get().isUnknown();
    }

    public static AStrategy get() {
        if (enemyStrategy == null) {
            return enemyStrategy = new UnknownStrategy();
        }

        return enemyStrategy;
    }

    protected static void setEnemyStrategy(AStrategy strategy) {
        enemyStrategy = strategy;
    }
    
}
