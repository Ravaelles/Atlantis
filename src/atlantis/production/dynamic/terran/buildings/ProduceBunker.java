package atlantis.production.dynamic.terran.buildings;

import atlantis.combat.micro.terran.bunker.TerranBunker;

public class ProduceBunker {
    public static boolean produce() {
        TerranBunker terranBunker = new TerranBunker();
        
        if (terranBunker.shouldBuildNew()) return terranBunker.requestToBuildNewAntiLandCombatBuilding();

        return false;
    }
}
