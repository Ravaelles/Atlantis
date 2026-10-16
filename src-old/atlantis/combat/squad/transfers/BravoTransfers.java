package atlantis.combat.squad.transfers;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.squads.bravo.Bravo;

public class BravoTransfers extends AbstractSquadTransfers {
    @Override
    public Squad squad() {
        return Bravo.get();
    }
}