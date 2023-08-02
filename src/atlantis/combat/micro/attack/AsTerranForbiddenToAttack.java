package atlantis.combat.micro.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AsTerranForbiddenToAttack extends HasUnit {
    public AsTerranForbiddenToAttack(AUnit unit) {
        super(unit);
    }

    public boolean isForbidden() {
//        if (unit.isMissionDefend()) {
//            Selection bunkers = Select.ourOfType(AUnitType.Terran_Bunker);
//            if (bunkers.empty()) {
//                return false;
//            }
//        }

        return false;
    }
}