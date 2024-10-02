package atlantis.combat.squad.transfers;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.AUnit;

public abstract class AbstractSquadTransfers {
    public abstract Squad squad();

    public void handleTransfers() {
        if (!squad().shouldHaveThisSquad()) {
            removeUnitsFromSquad(squad());
            return;
        }

        squad().handleReinforcements();
    }

    protected void removeUnitsFromSquad(Squad squad) {
        Squad transferToSquad = transferToSquad();

        for (int i = 0; i < squad.size(); i++) {
            AUnit transfer = squad.get(0);
            (new SquadReinforcements(transferToSquad)).transferUnitToSquad(transfer);
        }
    }

    private static Squad transferToSquad() {
        return Alpha.get();
    }
}
