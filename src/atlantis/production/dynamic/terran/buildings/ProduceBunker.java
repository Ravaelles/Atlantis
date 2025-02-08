package atlantis.production.dynamic.terran.buildings;

import atlantis.combat.micro.terran.bunker.TerranBunker;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Have;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class ProduceBunker {
    public static boolean produce() {
        if (shouldBuild()) {
            (new TerranBunker()).requestToBuildNewAntiLandCombatBuilding();
        }


        return false;
    }

    public static boolean shouldBuild() {
        if (!We.terran()) {
            ErrorLog.printMaxOncePerMinute("shouldBuildNew (Bunker) called for non-terran");
            return false;
        }

        if (!Have.barracks()) return false;

        return (new ShouldProduceNewBunker()).shouldBuild();
    }
}
