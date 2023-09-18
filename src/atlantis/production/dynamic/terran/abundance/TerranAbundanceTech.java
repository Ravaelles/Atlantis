package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.terran.tech.TerranInfantryWeapons;
import atlantis.units.select.Count;

public class TerranAbundanceTech extends Commander {
    @Override
    public boolean applies() {
        return A.canAfford(900, 400);
    }

    @Override
    protected void handle() {
        int marines = Count.marines();

        if (marines >= 9) {
            (new TerranInfantryWeapons()).forceHandle();
        }
    }
}
