package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.secure.SecuringBase;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class SecureBasesCommander extends Commander {
    private Selection bases;

    public SecureBasesCommander() {
    }

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(41)
            && Have.barracks()
            && Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) >= 2
            && CountInQueue.count(AUnitType.Terran_Bunker) <= 0;
    }

    @Override
    protected void handle() {
        int baseNumber = 1;
        bases = Select.ourBasesWithUnfinished();

        for (AUnit base : bases.list()) {
            if (baseNumber++ <= 1) continue;

            secureExistingBase(base);
        }
    }

    private void secureExistingBase(AUnit base) {
        if (!isBaseSecured(base)) secureBase(base);
    }

    private static boolean secureBase(AUnit base) {
        return (new SecuringBase(base.position())).secure();
    }

    protected boolean isBaseSecured(AUnit base) {
        if (!We.terran()) return true;

//        System.err.println("@ " + A.now() + " - isSecure base? " + base + " / " + (new SecuringBase(base.position())).isSecure());

        return (new SecuringBase(base.position())).isSecure();
    }
}
