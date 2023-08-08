package atlantis.combat.micro.terran;

import atlantis.architecture.Commander;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public class TerranBunkersInMain extends Commander {
    private TerranBunker terranBunker;

    public TerranBunkersInMain() {
        this.terranBunker = new TerranBunker();
    }

    @Override
    public boolean applies() {
        if (OurStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return false;
        }

        if (!EnemyInfo.isDoingEarlyGamePush()) {
            return false;
        }

        int existingBunkers = Count.existingOrInProductionOrInQueue(type());
        int expectedBunkers = terranBunker.expected();
        if (existingBunkers < expectedBunkers) {
            int neededBunkers = expectedBunkers - existingBunkers;

            for (int i = 0; i < neededBunkers; i++) {
                AddToQueue.maxAtATime(type(), neededBunkers);
//                System.err.println("Requested BUNKER");
            }
            return neededBunkers > 0;
        }

        return false;
    }

    @Override
    public void handle() {
        if (applies()) {
            // to-do: handle bunkers in main
        }
    }

    private static AUnitType type() {
        return Terran_Bunker;
    }
}
