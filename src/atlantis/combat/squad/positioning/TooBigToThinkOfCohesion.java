package atlantis.combat.squad.positioning;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class TooBigToThinkOfCohesion extends Manager {

    public TooBigToThinkOfCohesion(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        return A.supplyUsed() >= 150 && ArmyStrength.ourArmyRelativeStrength() >= 50;
    }
}

