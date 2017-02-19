package atlantis.strategy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AEnemyStrategy {
    
    private static final List<AEnemyStrategy> allStrategies = new ArrayList<>();
    
    public static final AEnemyStrategy TERRAN_BBS = new AEnemyStrategy();
    public static final AEnemyStrategy TERRAN_1_Rax_FE = new AEnemyStrategy();
    public static final AEnemyStrategy TERRAN_Double_Rax_MnM = new AEnemyStrategy();
    public static final AEnemyStrategy TERRAN_Tri_Rax_MnM = new AEnemyStrategy();
    public static final AEnemyStrategy TERRAN_Three_Factory_Vultures = new AEnemyStrategy();

    // Auto load this class
    private static final Object autoInitializer = autoInitialize();
    
    // =========================================================
    
    private static AEnemyStrategy enemyStrategy = null;
    
    // =========================================================
    
    private String name;
    private String url;
    private boolean terran = false;
    private boolean protoss = false;
    private boolean zerg = false;
    private boolean goingRush = false;
    private boolean goingAllInRush = false;
    private boolean goingExpansion = false;
    private boolean goingTech = false;
    
    // =========================================================

    private AEnemyStrategy() {
        allStrategies.add(this);
    }
//    private AEnemyStrategy(String name, String url) {
//        this.name = name;
//        this.url = url;
//    }
    
    // =========================================================
    
    /**
     * Executed on class load.
     */
    private static Object autoInitialize() {
        
        // Rushes
        TERRAN_Double_Rax_MnM.setTerran().setName("Double Rax MnM")
                .setGoingRush(true)
                .setUrl("http://strategywiki.org/wiki/StarCraft/Terran_strategies#Terran_Double_Rax_MnM");
        
        // All-in rushes
        TERRAN_BBS.setTerran().setName("BBS")
                .setGoingRush(true).setGoingAllInRush(true)
                .setUrl("http://wiki.teamliquid.net/starcraft/Barracks_Barracks_Supply_(vs._Terran)");
        TERRAN_Tri_Rax_MnM.setTerran().setName("Tri-Rax MnM Rush")
                .setGoingRush(true)
                .setUrl("http://strategywiki.org/wiki/StarCraft/Terran_strategies#Terran_Tri-Rax_MnM_Rush");
        
        // Expansion
        TERRAN_1_Rax_FE.setTerran().setName("1 Rax FE")
                .setGoingExpansion(true).setGoingTech(true)
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Rax_FE_(vs._Terran)");
        
        // Tech 
        TERRAN_Three_Factory_Vultures.setTerran().setName("Three Factory Vultures")
                .setGoingTech(true)
                .setUrl("http://wiki.teamliquid.net/starcraft/Three_Factory_Vultures");
        
        return true;
    }

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

    public AEnemyStrategy setGoingRush(boolean goingRush) {
        this.goingRush = goingRush;
        return this;
    }

    public boolean isGoingAllInRush() {
        return goingAllInRush;
    }

    public AEnemyStrategy setGoingAllInRush(boolean goingExtremeRush) {
        this.goingAllInRush = goingExtremeRush;
        return this;
    }

    public boolean isGoingExpansion() {
        return goingExpansion;
    }

    public AEnemyStrategy setGoingExpansion(boolean goingExpansion) {
        this.goingExpansion = goingExpansion;
        return this;
    }

    public boolean isGoingTech() {
        return goingTech;
    }

    public AEnemyStrategy setGoingTech(boolean goingTech) {
        this.goingTech = goingTech;
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
