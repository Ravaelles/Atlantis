package atlantis.debug.painter;

import atlantis.architecture.Commander;

public class PainterCommander extends Commander {

    @Override
    public void handle() {
        AAdvancedPainter.paint();
    }
}
