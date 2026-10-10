package atlantis.debug;

import atlantis.architecture.Commander;

public class DebugCommander extends Commander {
    @Override
    protected boolean handle() {
        // Inert unless OPENBW_PROBE=1 is in ENV; surveys the engine's answers on a
        // few early frames and stops. See OpenBwCapabilityProbe.
        OpenBwCapabilityProbe.update();
        return false;
    }
}
