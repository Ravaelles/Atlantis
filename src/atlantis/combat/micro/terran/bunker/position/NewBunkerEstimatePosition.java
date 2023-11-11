package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;
import atlantis.util.PauseAndCenter;
import bwapi.Color;

public class NewBunkerEstimatePosition {
    public HasPosition approximatePosition() {
        System.err.println("a = " + BunkerEstimatePositionAtNatural.define() + " / " + Select.main().position());
        if (ShouldPlaceAtNatural.shouldPlaceAtNatural()) return BunkerEstimatePositionAtNatural.define();

        HasPosition positionToSecure = Select.ourBases().last();
        PauseAndCenter.on(positionToSecure, true, Color.Teal);

        return BunkerEstimatePositionAtNonNatural.define(positionToSecure);
    }
}
