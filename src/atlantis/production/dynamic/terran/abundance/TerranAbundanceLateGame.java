package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;

public class TerranAbundanceLateGame extends Commander {
    @Override
    public boolean applies() {
        return A.hasMinerals(900)
            && GamePhase.isLateGame()
            && A.hasFreeSupply(4);
    }

    @Override
    protected boolean handle() {
        AbundanceProduce.produceTank();
        AbundanceProduce.produceMarine();
        AbundanceProduce.produceMarine();
        return false;
    }
}
