package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldFightInsteadAvoidAsFirebat extends Manager {
    public ShouldFightInsteadAvoidAsFirebat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isFirebat() && unit.hp() >= 40 && unit.friendsInRadiusCount(3) >= 4;
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
