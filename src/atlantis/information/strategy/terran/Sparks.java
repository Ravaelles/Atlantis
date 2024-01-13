package atlantis.information.strategy.terran;

import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.strategy.AStrategy;

public class Sparks extends AStrategy {
    public Sparks() {
        setTerran().setName("Sparks").setGoingBio().setGoingRush();
    }

    @Override
    public void applyDecisions() {
//        TerranDecisions.DONT_PRODUCE_TANKS_AT_ALL.setCurrentValue(true);
    }
}
