package atlantis.debug.painter;

import atlantis.architecture.Commander;

public class PainterCommander extends Commander {
    @Override
    protected void handle() {
        AAdvancedPainter.paint();
    }
}
