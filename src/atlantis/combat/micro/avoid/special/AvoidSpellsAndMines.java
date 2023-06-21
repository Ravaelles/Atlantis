package atlantis.combat.micro.avoid.special;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;


public class AvoidSpellsAndMines {

    public static boolean avoidSpellsAndMines(AUnit unit) {

        // === Psionic Storm ========================================

        if (AvoidPsionicStorm.avoidPsionicStorm(unit)) return true;

        // === Mines ===============================================

        return AvoidMines.handleMines(unit);
    }
}
