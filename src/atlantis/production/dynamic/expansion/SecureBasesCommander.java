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

import java.util.List;

public class SecureBasesCommander extends Commander {
    private static SecuringBase securingBase;
    private Selection bases;

    public SecureBasesCommander() {
    }

    @Override
    public boolean applies() {
        if (true) return false;

        return A.everyNthGameFrame(53)
            && Have.barracks()
            && Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) >= 2
            && Count.withPlanned(AUnitType.Terran_Bunker) <= Count.basesWithPlanned()
            && CountInQueue.count(AUnitType.Terran_Bunker, 10) <= 0;
    }

    @Override
    protected void handle() {
        int baseNumber = 0;
        bases = Select.ourBasesWithUnfinished();
        List<AUnit> basesReversed = bases.reverse().list();

        for (AUnit base : basesReversed) {
            if (baseNumber++ >= bases.count() - 1 && A.seconds() <= 1000) continue; // Skip for main

            securingBase = (new SecuringBase(base.position()));

            secureBaseWithCombatBuildings();
        }
    }

    private void secureBaseWithCombatBuildings() {
        if (!isBaseSecured()) secureBase();
    }

    private static boolean secureBase() {
        return securingBase.secureWithCombatBuildings();
    }

    protected boolean isBaseSecured() {
        if (!We.terran()) return true;

//        System.err.println("@ " + A.now() + " - isSecure base? " + base + " / " + (new SecuringBase(base.position())).isSecure());

        return securingBase.isSecure();
    }
}
