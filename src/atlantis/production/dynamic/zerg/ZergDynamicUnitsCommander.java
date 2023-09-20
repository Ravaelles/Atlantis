
package atlantis.production.dynamic.zerg;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.zerg.units.ProduceHydras;
import atlantis.production.dynamic.zerg.units.ProduceMutas;
import atlantis.production.dynamic.zerg.units.ProduceZerglings;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ZergDynamicUnitsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.zerg();
    }

    @Override
    protected void handle() {
        if (Count.larvas() == 0) return;

        ProduceMutas.mutalisks();

        if (Count.larvas() <= 1) return;
        
        ProduceHydras.hydras();
        ProduceZerglings.zerglings();
    }
}
