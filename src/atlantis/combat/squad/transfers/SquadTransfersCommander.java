package atlantis.combat.squad.transfers;

import atlantis.architecture.Commander;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.units.AUnit;

public class SquadTransfersCommander extends Commander {

    @Override
    protected boolean handle() {
        updateSquadTransfers();
        return false;
    }

    private void updateSquadTransfers() {
        (new OmegaTransfers()).handleTransfers();
        (new BravoTransfers()).handleTransfers();
    }

    public static void removeUnitFromSquads(AUnit unit) {
        Squad squad = unit.squad();

        if (squad != null) {
            squad.removeUnit(unit);
            unit.forceSetSquad(null);
        }

        Alpha.get().removeUnit(unit);
    }
}
