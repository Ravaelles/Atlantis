package atlantis.combat.micro.terran.bunker.listeners;

import atlantis.combat.micro.terran.bunker.NewBunker;
import atlantis.game.A;
import atlantis.game.event.Listener;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class OurBunkerDestroyed extends Listener {
    @Override
    public String listensTo() {
        return "OurBunkerDestroyed";
    }

    @Override
    public void onEvent(String event, Object... data) {
        System.err.println(A.minSec() + " Our bunker was destroyed!");

        NewBunker newBunker = new NewBunker();

        if (Count.basesWithUnfinished() <= 1) newBunker.requestNewBunker(Select.mainOrAnyBuilding());
        else newBunker.requestNewAndAutomaticallyDecidePosition();
    }
}
