package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class DebugIdleUnitsManager extends Manager {
    public DebugIdleUnitsManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.looksIdle() && !skip();
    }

    private boolean skip() {
        return unit.isMedic();
    }

    @Override
    public Manager handle() {
        System.out.println("@ " + A.now()
            + " - IDLE - "
            + unit + " / "
            + unit.manager() + " / "
            + unit.getLastCommand().getType()
        );

        return null;
    }
}

