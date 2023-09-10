package atlantis.information.strategy;

import atlantis.information.enemy.EnemyUnits;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.ABuildOrderLoader;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.List;


public class AStrategy {

    private static final List<AStrategy> allStrategies = new ArrayList<>();

    // =========================================================

    private String name;
    private String url;
    private ABuildOrder buildOrder = null;
    private boolean terran = false;
    private boolean protoss = false;
    private boolean zerg = false;
    private boolean goingBio = false;
    private boolean goingRush = false;
    private boolean goingCheese = false;
    private boolean goingExpansion = false;
    private boolean goingTech = false;
    private boolean goingHiddenUnits = false;
    private boolean goingAirUnitsQuickly = false;
    private boolean goingAirUnitsLate = false;
    protected boolean unknown = false;

    // =========================================================

    protected AStrategy() {
        allStrategies.add(this);
    }

    protected static AStrategy protossStrategy() {
        return (new AStrategy()).setProtoss();
    }

    // =========================================================

    protected static boolean has(AUnitType type) {
        return EnemyUnits.has(type);
    }

    protected static int count(AUnitType type) {
        return EnemyUnits.count(type);
    }

    /**
     * Assigns build order for this strategy from build orders directory. Uses corresponding strategy/build order name.
     */
    private void assignBuildOrder() {
        buildOrder = ABuildOrderLoader.getBuildOrderForStrategy(this);
        CurrentBuildOrder.set(buildOrder);
    }

    @Override
    public String toString() {
        return race() + ":" + name;
    }

    // =========================================================

    public String name() {
        return name;
    }

    public AStrategy setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AStrategy setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isRushOrCheese() {
        return goingRush || goingCheese;
    }

    public boolean isRush() {
        return goingRush;
    }

    public AStrategy setGoingRush() {
        this.goingRush = true;
        return this;
    }

    public boolean isGoingCheese() {
        return goingCheese;
    }

    public AStrategy setGoingCheese() {
        this.goingCheese = true;
        return this;
    }

    public boolean isExpansion() {
        return goingExpansion;
    }

    public AStrategy setGoingExpansion() {
        this.goingExpansion = true;
        return this;
    }

    public boolean isGoingTech() {
        return goingTech;
    }

    public AStrategy setGoingTech() {
        this.goingTech = true;
        return this;
    }

    public boolean isGoingHiddenUnits() {
        return goingHiddenUnits;
    }

    public AStrategy setGoingHiddenUnits() {
        this.goingHiddenUnits = true;
        return this;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public boolean isAirUnits() {
        return goingAirUnitsQuickly;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public AStrategy setGoingAirUnitsQuickly() {
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
    public AStrategy setGoingAirUnitsLate() {
        this.goingAirUnitsLate = true;
        return this;
    }

    public AStrategy setGoingBio() {
        this.goingBio = true;
        return this;
    }

    public boolean goingBio() {
        return this.goingBio;
    }

    public boolean isTerran() {
        return terran;
    }

    public AStrategy setTerran() {
        this.terran = true;
        return this;
    }

    public boolean isProtoss() {
        return protoss;
    }

    public AStrategy setProtoss() {
        this.protoss = true;
        return this;
    }

    public boolean isZerg() {
        return zerg;
    }

    public AStrategy setZerg() {
        this.zerg = true;
        return this;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public ABuildOrder buildOrder() {
        if (buildOrder == null) {
            assignBuildOrder();
        }

        return buildOrder;
    }

    public String race() {
        if (isProtoss()) {
            return "Protoss";
        }
        if (isTerran()) {
            return "Terran";
        }
        else {
            return "Zerg";
        }
    }
}
