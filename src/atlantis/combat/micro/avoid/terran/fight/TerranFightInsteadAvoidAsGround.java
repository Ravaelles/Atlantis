package atlantis.combat.micro.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.wraith.TerranWraith;
import atlantis.units.AUnit;

public class TerranFightInsteadAvoidAsGround extends Manager {
    public TerranFightInsteadAvoidAsGround(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit()
            && !unit.isTank()
            && unit.cooldownRemaining() <= 2
            && unit.hpMoreThan(20)
            && unit.friendsNear().tanks().inRadius(8, unit).atLeast(1);
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
