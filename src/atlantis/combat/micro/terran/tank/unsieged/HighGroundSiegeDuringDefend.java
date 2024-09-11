package atlantis.combat.micro.terran.tank.unsieged;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class HighGroundSiegeDuringDefend extends Manager {
    public HighGroundSiegeDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankUnsieged() && unit.isMissionDefend();
    }

    public Manager handle() {
//        if () {
//            return usedManager(this);
//        }

        return null;
    }
}
