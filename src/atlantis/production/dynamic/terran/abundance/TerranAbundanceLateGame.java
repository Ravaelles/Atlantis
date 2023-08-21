package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;

public class TerranAbundanceLateGame extends Commander {
    @Override
    public boolean applies() {
        return GamePhase.isLateGame() && A.hasFreeSupply(4);
    }

    @Override
    protected void handle() {
    }
}
