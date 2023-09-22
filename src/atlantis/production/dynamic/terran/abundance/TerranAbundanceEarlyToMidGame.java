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
        if (Abundance.ifNotNullProduce(Abundance.freeStarport(), Terran_Wraith)) return;
        if (Abundance.ifNotNullProduce(Abundance.freeFactoryWithMachineShop(), Terran_Siege_Tank_Tank_Mode)) return;
        if (Abundance.ifNotNullProduce(Abundance.freeFactory(), Terran_Vulture)) return;
        if (Abundance.ifNotNullProduce(Abundance.freeBarracks(), Terran_Marine)) return;
    }
}
