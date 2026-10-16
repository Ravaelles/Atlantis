package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ProtossPreventIssuingTooRapidCommands extends Manager {
    public ProtossPreventIssuingTooRapidCommands(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        int commandIssuedAgo = unit.lastCommandIssuedAgo();

//        return commandIssuedAgo <= 1 || (unit.isHoldingPosition() && commandIssuedAgo <= 3);
        return commandIssuedAgo <= 1;
//        return commandIssuedAgo <= 1 || (commandIssuedAgo <= 10 && !unit.isStopped());
//        return commandIssuedAgo <= 3 || (commandIssuedAgo <= 10 && !unit.isStopped());
    }

    @Override
    public Manager handle() {
        return usedManager(unit.manager());
    }
}
