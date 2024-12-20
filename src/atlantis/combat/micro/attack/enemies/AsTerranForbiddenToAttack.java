package atlantis.combat.micro.attack.enemies;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

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