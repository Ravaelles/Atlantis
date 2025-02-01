package atlantis.game.init;

import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.production.dynamic.terran.tech.ResearchStimpacks;
import atlantis.production.dynamic.terran.tech.ResearchU238;

public class DetectInitialTechs {
    public static void update() {
        // Protoss
        if (ATech.isResearched(ResearchSingularityCharge.upgrade())) ResearchSingularityCharge.onResearched();

        // Terran
        if (ATech.isResearched(ResearchU238.what())) ResearchU238.onResearched();
        if (ATech.isResearched(ResearchStimpacks.what())) ResearchStimpacks.onResearched();
    }
}
