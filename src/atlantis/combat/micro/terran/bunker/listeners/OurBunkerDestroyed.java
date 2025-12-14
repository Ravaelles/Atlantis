package atlantis.combat.micro.terran.bunker.listeners;

import atlantis.combat.micro.terran.bunker.NewBunker;
import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class OurBunkerDestroyed extends AutomaticListener {
    @Override
    public Event listensTo() {
        return Event.OUR_BUNKER_DESTROYED;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        System.err.println(A.minSec() + " Our bunker was destroyed!");

        NewBunker newBunker = new NewBunker();

        if (Count.basesWithUnfinished() <= 1) newBunker.requestNewBunker(Select.mainOrAnyBuilding());
        else newBunker.requestNewAndAutomaticallyDecidePosition();
    }
}
