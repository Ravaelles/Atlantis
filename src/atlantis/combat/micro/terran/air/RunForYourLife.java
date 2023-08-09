package atlantis.combat.micro.terran.air;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;

public class RunForYourLife extends Manager {
    public RunForYourLife(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && unit.hp() <= 40;
    }

    @Override
    protected Manager handle() {
        if ((new AvoidEnemies(unit)).invoke() != null) return usedManager(this);

        return null;
    }
}
