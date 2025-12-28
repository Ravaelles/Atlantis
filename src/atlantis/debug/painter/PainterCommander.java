package atlantis.debug.painter;

import atlantis.architecture.Commander;

public class PainterCommander extends Commander {
    @Override
    protected boolean handle() {
        AAdvancedPainter.paint();
        return false;
    }

    @Override
    public boolean shouldProfile() {
        return false;
    }
}
