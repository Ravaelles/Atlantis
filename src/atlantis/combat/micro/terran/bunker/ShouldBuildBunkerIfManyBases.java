package atlantis.combat.micro.terran.bunker;

import atlantis.units.select.Count;

public class ShouldBuildBunkerIfManyBases {
    public static boolean shouldBuild() {
        return TerranBunker.existingOrInProduction() < Count.basesWithUnfinished();
    }
}
