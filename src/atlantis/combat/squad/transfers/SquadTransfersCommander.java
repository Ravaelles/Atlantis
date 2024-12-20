package atlantis.combat.squad.transfers;

import atlantis.architecture.Commander;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.AUnit;

public class SquadTransfersCommander extends Commander {

    @Override
    protected void handle() {
        updateSquadTransfers();
    }

    private void updateSquadTransfers() {
        (new OmegaTransfers()).handleTransfers();
        (new BravoTransfers()).handleTransfers();
    }

    public static void removeUnitFromSquads(AUnit unit) {
        Squad squad = unit.squad();

        if (squad != null) {
            squad.removeUnit(unit);
            unit.setSquad(null);
        }

        Alpha.get().removeUnit(unit);
    }
}
