package atlantis.production.dynamic.terran.reinforce;

import atlantis.architecture.Commander;
import atlantis.map.position.HasPosition;

public class TerranReinforceBase extends Commander {
    private HasPosition position;

    public TerranReinforceBase(HasPosition position) {
        this.position = position;
    }

    @Override
    public boolean applies() {
        return position != null;
    }

    @Override
    protected void handle() {
        super.handle();
    }
}
