package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.wraith.TerranWraith;
import atlantis.units.AUnit;

public class ShouldFightInsteadAvoidAsWraith extends Manager {
    public ShouldFightInsteadAvoidAsWraith(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith()
            && unit.hp() >= 30
            && unit.combatEvalRelative() > 0.95
            && TerranWraith.noAntiAirBuildingNearby(unit);
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
