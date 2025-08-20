package atlantis.combat.squad.transfers;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.squads.omega.Omega;

public class OmegaTransfers extends AbstractSquadTransfers {
    @Override
    public Squad squad() {
        return Omega.get();
    }
}