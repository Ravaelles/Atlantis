package atlantis.combat.micro.avoid.special;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;


public class AvoidSpellsAndMines {

    public  boolean avoidSpellsAndMines() {

        // === Psionic Storm ========================================

        if (AvoidPsionicStorm.avoidPsionicStorm()) return true;

        // === Mines ===============================================

        return AvoidMines.handleMines();
    }
}
