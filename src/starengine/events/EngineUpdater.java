package starengine.events;

import starengine.StarEngine;

public class EngineUpdater {
    public static void update(StarEngine engine) {
//        System.out.println("@ " + A.now() + " - FRAME - update units (" + engine.units.getClass().getSimpleName() + ")");
        engine.units.updateUnits();

        if (simulateFrameTime()) waitAfterFrameEnded();

        engine.game().checkForGameEnd();
    }

    private static boolean simulateFrameTime() {
        if (true) return false;
        return true;
    }

    private static void waitAfterFrameEnded() {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
        }
    }
}
