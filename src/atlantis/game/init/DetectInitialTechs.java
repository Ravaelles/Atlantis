package atlantis.game.init;

import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;

public class DetectInitialTechs {
    public static void update() {
        if (ATech.isResearched(ResearchSingularityCharge.upgrade())) ResearchSingularityCharge.onResearched();
    }
}
