package atlantis.strategy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AEnemyStrategy {
    
    private static final List<AEnemyStrategy> allStrategies = new ArrayList<>();
    
    // =========================================================
    
    private static AEnemyStrategy enemyStrategy = null;
    
    // =========================================================
    
    private String name;
    private String url;
    private boolean terran = false;
    private boolean protoss = false;
    private boolean zerg = false;
    private boolean goingRush = false;
    private boolean goingCheese = false;
    private boolean goingExpansion = false;
    private boolean goingTech = false;
    private boolean goingHiddenUnits = false;
    private boolean goingAirUnitsQuickly = false;
    private boolean goingAirUnitsLate = false;
    
    // =========================================================

    protected AEnemyStrategy() {
        allStrategies.add(this);
    }
    
    // =========================================================

    @Override
    public String toString() {
        return name;
    }
    
    // =========================================================

    public static boolean isEnemyStrategyKnown() {
        return enemyStrategy != null;
    }

    public static AEnemyStrategy getEnemyStrategy() {
        return enemyStrategy;
    }

    protected static void setEnemyStrategy(AEnemyStrategy enemyStrategy) {
        AEnemyStrategy.enemyStrategy = enemyStrategy;
    }

    public String getName() {
        return name;
    }

    public AEnemyStrategy setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AEnemyStrategy setUrl(String url) {
        this.url = url;
        return this;
    }
    
    public boolean isGoingRush() {
        return goingRush;
    }

    public AEnemyStrategy setGoingRush() {
        this.goingRush = true;
        return this;
    }

    public boolean isGoingCheese() {
        return goingCheese;
    }

    public AEnemyStrategy setGoingCheese() {
        this.goingCheese = true;
        return this;
    }

    public boolean isGoingExpansion() {
        return goingExpansion;
    }

    public AEnemyStrategy setGoingExpansion() {
        this.goingExpansion = true;
        return this;
    }

    public boolean isGoingTech() {
        return goingTech;
    }

    public AEnemyStrategy setGoingTech() {
        this.goingTech = true;
        return this;
    }

    public boolean isGoingHiddenUnits() {
        return goingHiddenUnits;
    }

    public AEnemyStrategy setGoingHiddenUnits() {
        this.goingHiddenUnits = true;
        return this;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public boolean isGoingAirUnitsQuickly() {
        return goingAirUnitsQuickly;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public AEnemyStrategy setGoingAirUnitsQuickly() {
        this.goingAirUnitsQuickly = true;
        return this;
    }

    /**
     * Late units are: Carrier, Guardian, Battlecruiser.
     */
    public boolean isGoingAirUnitsLate() {
        return goingAirUnitsLate;
    }

    /**
     * Late units are: Carrier, Guardian, Battlecruiser.
     */
    public AEnemyStrategy setGoingAirUnitsLate() {
        this.goingAirUnitsLate = true;
        return this;
    }

    public boolean isTerran() {
        return terran;
    }

    public AEnemyStrategy setTerran() {
        this.terran = true;
        return this;
    }

    public boolean isProtoss() {
        return protoss;
    }

    public AEnemyStrategy setProtoss() {
        this.protoss = true;
        return this;
    }

    public boolean isZerg() {
        return zerg;
    }

    public AEnemyStrategy setZerg() {
        this.zerg = true;
        return this;
    }
    
}
