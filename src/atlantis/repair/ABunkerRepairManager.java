package atlantis.repair;

import atlantis.units.AUnit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ABunkerRepairManager {

    public static void updateBunkerRepairer(AUnit repairer) {
        AUnit bunker = ARepairManager.getConstantBunkerToRepairFor(repairer);
        if (bunker != null) {
            repairer.setTooltip("Protect " + bunker.getShortNamePlusId());
            repairer.repair(bunker);
        }
        else {
            repairer.setTooltip("Null bunker");
            ARepairManager.removeConstantBunkerRepairer(repairer);
        }
    }
    
}
