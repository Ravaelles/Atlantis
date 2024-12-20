package atlantis.combat.squad.omega;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.transfers.SquadReinforcements;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class OmegaReinforcements extends SquadReinforcements {
    public OmegaReinforcements(Squad toSquad) {
        super(toSquad);
    }

    @Override
    protected boolean isGoodRecruit(AUnit recruit) {
        Omega omega = Omega.get();

        if (We.terran() && omega.units().tanks().empty() && Count.tanks() >= 6) {
            return recruit.isTank();
        }

        if (We.protoss() && Alpha.get().units().zealots().notEmpty()) {
            return recruit.isZealot();
        }

        return true;
    }
}
