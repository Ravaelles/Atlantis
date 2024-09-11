package atlantis.combat.squad.beta;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.transfers.SquadReinforcements;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class BetaReinforcements extends SquadReinforcements {
    public BetaReinforcements(Squad toSquad) {
        super(toSquad);
    }

    @Override
    protected boolean isGoodRecruit(AUnit recruit) {
        Beta beta = Beta.get();

        if (beta.units().tanks().empty() && Count.tanks() >= 6) {
            return recruit.isTank();
        }

        return true;
    }
}
