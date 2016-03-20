package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import static atlantis.constructing.AtlantisConstructingManager.requestConstructionOf;
import atlantis.wrappers.SelectUnits;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisExpansionManager {

    public static void requestNewBaseIfNeeded() {
        boolean haveEnoughMinerals = AtlantisGame.hasMinerals(490)
                || (AtlantisGame.playsAsZerg() && AtlantisGame.hasMinerals(342));
        boolean haveEnoughBases = SelectUnits.ourBases().count() >= 7 
                && (!AtlantisGame.playsAsZerg() && SelectUnits.ourLarva().count() >= 2);
        boolean areWeAlreadyExpanding = 
                AtlantisConstructingManager.countNotStartedConstructionsOfType(AtlantisConfig.BASE) == 0;
        boolean allowExtraExpansion = AtlantisGame.hasMinerals(650) &&
                AtlantisConstructingManager.countNotStartedConstructionsOfType(AtlantisConfig.BASE) <= 1;
        if (haveEnoughMinerals && !haveEnoughBases && (!areWeAlreadyExpanding || allowExtraExpansion)) {
            requestConstructionOf(AtlantisConfig.BASE);
        }
    }
    
}
