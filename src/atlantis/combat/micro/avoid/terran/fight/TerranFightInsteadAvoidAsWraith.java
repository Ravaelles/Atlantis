package atlantis.combat.micro.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.wraith.TerranWraith;
import atlantis.units.AUnit;

public class TerranFightInsteadAvoidAsWraith extends Manager {
    public TerranFightInsteadAvoidAsWraith(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith()
            && (unit.hp() >= 30 || unit.enemiesNear().onlyAir())
            && unit.eval() >= 0.90
            && TerranWraith.noAntiAirBuildingNearby(unit);
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
