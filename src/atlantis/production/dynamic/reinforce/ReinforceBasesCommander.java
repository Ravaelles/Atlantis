package atlantis.production.dynamic.reinforce;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.secure.terran.SecuringBaseAsTerran;
import atlantis.production.dynamic.protoss.ProtossSecureBasesCommander;
import atlantis.production.dynamic.reinforce.terran.TerranReinforceBasesWithCombatBuildings;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

import java.util.List;

public class ReinforceBasesCommander extends Commander {
    private static SecuringBaseAsTerran securingBase;
    private Selection bases;

    public ReinforceBasesCommander() {
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProtossSecureBasesCommander.class,
            TerranReinforceBasesWithCombatBuildings.class,
        };
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        return A.everyNthGameFrame(43);
    }

    @Override
    protected void handle() {
        int baseNumber = 0;
        bases = Select.ourBasesWithUnfinished();
        List<AUnit> basesReversed = bases.reverse().list();

        for (AUnit base : basesReversed) {
            if (baseNumber++ >= bases.count() - 1 && A.seconds() <= 1000) continue; // Skip for main

            securingBase = (new SecuringBaseAsTerran(base.position()));

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

//        System.err.println("@ " + A.now() + " - isSecure base? " + base + " / " + (new SecuringBaseAsTerran(base.position())).isSecure());

        return securingBase.isSecure();
    }
}
