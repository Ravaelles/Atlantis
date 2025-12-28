package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;

public class TerranAbundanceEarlyToMidGame extends Commander {
    @Override
    public boolean applies() {
        return !GamePhase.isLateGame() && A.hasFreeSupply(3) && A.hasMinerals(800);
    }

    @Override
    protected boolean handle() {
        if (AbundanceProduce.produceWraith()) ;
        if (AbundanceProduce.produceTank()) ;
        if (AbundanceProduce.produceMarine()) ;
        if (AbundanceProduce.produceVulture()) ;
        return false;
    }
}
