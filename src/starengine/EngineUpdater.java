package starengine;

import atlantis.game.A;

public class EngineUpdater {
    public static void update(StarEngine engine) {
        System.out.println("@ " + A.now() + " - FRAME - update units (" + engine.units.getClass().getSimpleName() + ")");
        engine.units.updateUnits();
    }
}
