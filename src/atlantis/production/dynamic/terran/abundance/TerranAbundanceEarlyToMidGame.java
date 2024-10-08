package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;

import static atlantis.units.AUnitType.*;

public class TerranAbundanceEarlyToMidGame extends Commander {
    @Override
    public boolean applies() {
        return !GamePhase.isLateGame() && A.hasFreeSupply(3) && A.hasMinerals(800);
    }

    @Override
    protected void handle() {
        if (AbundanceProduce.produceWraith()) ;
        if (AbundanceProduce.produceTank()) ;
        if (AbundanceProduce.produceMarine()) ;
        if (AbundanceProduce.produceVulture()) ;
    }
}
