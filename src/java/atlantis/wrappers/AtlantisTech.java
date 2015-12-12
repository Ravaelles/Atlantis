package atlantis.wrappers;

import atlantis.AtlantisGame;
import jnibwapi.types.TechType;
import jnibwapi.types.UpgradeType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisTech {

    public static boolean isResearched(Object techOrUpgrade) {
        if (techOrUpgrade instanceof TechType) {
            return AtlantisGame.getPlayerUs().isResearched((TechType) techOrUpgrade);
        } else {
            return AtlantisGame.getPlayerUs().getUpgradeLevel((UpgradeType) techOrUpgrade) != 1;
        }
    }

}
