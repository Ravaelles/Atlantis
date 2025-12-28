
package atlantis.production.dynamic.zerg;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.zerg.units.ProduceHydras;
import atlantis.production.dynamic.zerg.units.ProduceMutas;
import atlantis.production.dynamic.zerg.units.ProduceZerglings;
import atlantis.units.select.Count;
import atlantis.util.HasReason;
import atlantis.util.We;

public class ZergDynamicUnitsCommander extends Commander implements HasReason {
    public static String reason = "-";

    @Override
    public boolean applies() {
        return We.zerg();
    }

    @Override
    protected boolean handle() {
        if (Count.larvas() == 0) return false;

        ProduceMutas.mutalisks();

        if (Count.larvas() <= 1) return false;

        ProduceHydras.hydras();
        ProduceZerglings.zerglings();
        return false;
    }

    @Override
    public String reason() {
        return reason;
    }
}
