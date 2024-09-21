package atlantis.game.events;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.profiler.CodeProfiler;
import atlantis.debug.profiler.LongFrames;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.GameSpeed;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.util.log.ErrorLog;

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
            AGame.calcSeconds();
            AGame.cacheFrameNow();
            Atlantis.getInstance().getGameCommander().invokeCommander();
        }

        // === Catch any exception that occur not to "kill" the bot with one trivial error ===================
        catch (Exception e) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("### AN ERROR HAS OCCURRED ###");
            System.err.println("### AN ERROR HAS OCCURRED ###");
            e.printStackTrace();
//            if (true) throw e;
        }

        if (A.notUms() && A.now() == 1) {
            CurrentBuildOrder.get().print();
        }

        OnEveryFrameHelper.handle();

        CodeProfiler.endMeasuringTotalFrame();
        LongFrames.reportFrameLength(CodeProfiler.lastFrameLength());

//        CodeProfiler.printSummary();

//        APosition natural = Bases.natural();
//        if (natural != null) {
//            APosition highGround = FindHighGround.findNear(natural, 10);
//            System.err.println("natural = " + natural + ", highGround " + highGround);
//            if (highGround != null) {
//                CameraCommander.centerCameraOn(highGround);
//                AAdvancedPainter.paintCircleFilled(highGround, 8, Color.Purple);
//            }
//        }
    }
}
