package atlantis.combat.squad.transfers;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.omega.Omega;
import atlantis.units.AUnit;

public class OmegaTransfers extends AbstractSquadTransfers {
    @Override
    public Squad squad() {
        return Omega.get();
    }
}