package atlantis.wrappers;

import atlantis.AtlantisGame;
import atlantis.production.ProductionOrder;
import bwapi.TechType;
import bwapi.UpgradeType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisTech {

    public static boolean isResearched(TechType tech) {
        return isResearchedTech(tech);
    }

    public static boolean isResearched(Object techOrUpgrade, ProductionOrder order) {
        if (techOrUpgrade instanceof TechType) {
            TechType tech = (TechType) techOrUpgrade;
            return isResearchedTech(tech);
        }
        else {
            int level = 1;
            if (order.getModifier() != null) {
                try {
                    level = Integer.parseInt(order.getModifier());
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            UpgradeType upgrade = (UpgradeType) techOrUpgrade;
            return isResearchedUpgrade(upgrade, level);
        }
    }
    
    // =========================================================
    
    /**
     * Returns level of given upgrade. 0 is initially, it can raise up to 3.
     */
    public static int getUpgradeLevel(UpgradeType upgrade) {
        return AtlantisGame.getPlayerUs().getUpgradeLevel(upgrade);
    }
    
    // =========================================================

    private static boolean isResearchedTech(TechType tech) {
        return AtlantisGame.getPlayerUs().hasResearched(tech);
    }
    
    private static boolean isResearchedUpgrade(UpgradeType upgrade, int expectedUpgradeLevel) {
        return getUpgradeLevel(upgrade) >= Math.min(expectedUpgradeLevel, 3);
    }

}
