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
    private static SecuringBase securingBase;
    private Selection bases;

    public SecureBasesCommander() {
    }

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(51)
            && Have.barracks()
            && Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) >= 2
            && CountInQueue.count(AUnitType.Terran_Bunker, 6) <= 2;
    }

    @Override
    protected void handle() {
        int baseNumber = 1;
        bases = Select.ourBasesWithUnfinished();

        for (AUnit base : bases.list()) {
            if (baseNumber++ <= 1) continue;

            securingBase = (new SecuringBase(base.position()));

            secureExistingBase();
        }
    }

    private void secureExistingBase() {
        if (!isBaseSecured()) secureBase();
    }

    private static boolean secureBase() {
        return securingBase.secure();
    }

    protected boolean isBaseSecured() {
        if (!We.terran()) return true;

//        System.err.println("@ " + A.now() + " - isSecure base? " + base + " / " + (new SecuringBase(base.position())).isSecure());

        return securingBase.isSecure();
    }
}
