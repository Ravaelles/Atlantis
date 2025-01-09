package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class NewBunkerApproximatePosition {
    public HasPosition approximatePosition() {
        if (ShouldPlaceBunkerAtNatural.shouldPlaceAtNatural()) return BunkerEstimatePositionAtNatural.define();

        if (Count.basesWithUnfinished() <= 1) {
            return forMainOrMainChoke();
        }

        if (Count.basesWithUnfinished() <= 2) {
            return BunkerEstimatePositionAtNatural.define();
        }

        return BunkerEstimatePositionAtNonNatural.define(Select.ourBasesWithUnfinished().last());
    }

    private HasPosition forMainOrMainChoke() {
        APosition main = Select.mainOrAnyBuildingPosition();

        HasPosition mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return main;

        return mainChoke.translateTilesTowards(7, main);
    }
}
