package atlantis.game;

import atlantis.Atlantis;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.profiler.CodeProfiler;
import atlantis.debug.profiler.LongFrames;
import atlantis.map.high.FindHighGround;
import atlantis.map.position.APosition;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnit;

public class OnEveryFrame {
    public static void update() {
        CodeProfiler.startMeasuringTotalFrame();

        // === Handle PAUSE ================================================
        // If game is paused wait 100ms - pause is handled by PauseBreak button
        while (GameSpeed.isPaused()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        // === All game actions that take place every frame ==================================================

        try {
            AGame.cacheFrameNow();
            Atlantis.getInstance().getGameCommander().invoke();
        }

        // === Catch any exception that occur not to "kill" the bot with one trivial error ===================
        catch (Exception e) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("### AN ERROR HAS OCCURRED ###");
//            A.printStackTrace("### AN ERROR HAS OCCURRED ###");
            System.err.println("### AN ERROR HAS OCCURRED ###");
//            if (true) throw e;
            e.printStackTrace();
        }

        if (A.notUms() && A.now() == 1) {
            CurrentBuildOrder.get().print();
        }

        OnEveryFrameHelper.handle();

        CodeProfiler.endMeasuringTotalFrame();
        LongFrames.reportFrameLength(CodeProfiler.lastFrameLength());

//        CodeProfiler.printSummary();

//        if (A.everyNthGameFrame(30)) {
//            AUnit leader = Alpha.get().leader();
//            if (leader != null) {
//                APosition near = FindHighGround.findNear(leader, 10);
//                System.err.println("leader = " + leader + ", near " + near);
//            }
//        }
    }
}
