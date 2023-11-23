package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranAbundanceLateGame extends Commander {
    @Override
    public boolean applies() {
        return A.hasMinerals(800)
            && GamePhase.isLateGame()
            && A.hasFreeSupply(4);
    }

    @Override
    protected void handle() {
        if (A.hasMinerals(900)) {
            AbundanceProduce.produceTank();
            AbundanceProduce.produceMarine();
            AbundanceProduce.produceMarine();
        }
    }
}
