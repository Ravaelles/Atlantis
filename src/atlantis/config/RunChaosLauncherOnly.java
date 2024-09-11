package atlantis.config;

import atlantis.game.A;
import atlantis.util.ProcessHelper;

public class RunChaosLauncherOnly {
    /**
     * Useful when you want to run ChaosLauncher only, without running your bot.
     * It can be used to run ChaosLauncher with another bot, assuming you set
     */
    public static boolean run() {
//        ProcessHelper.killStarcraftProcess();
//        ProcessHelper.killChaosLauncherProcess();

//        ProcessHelper.startChaosLauncherProcess();

        A.sleep(3500);

        return true;
    }
}
