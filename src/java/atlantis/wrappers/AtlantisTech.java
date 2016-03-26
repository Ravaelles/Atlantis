package atlantis.wrappers;

import atlantis.AtlantisGame;
import bwapi.TechType;
import bwapi.UpgradeType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisTech {

    public static boolean isResearched(Object techOrUpgrade) {
        if (techOrUpgrade instanceof TechType) {
            return AtlantisGame.getPlayerUs().hasResearched((TechType) techOrUpgrade);	//replaces isResearched
        } else {
            return AtlantisGame.getPlayerUs().getUpgradeLevel((UpgradeType) techOrUpgrade) != 1;
        }
    }

}
