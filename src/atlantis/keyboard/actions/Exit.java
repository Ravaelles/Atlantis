package atlantis.keyboard.actions;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;
import atlantis.util.ProcessHelper;

/**
 * "Exit was requested manually" (Escape in a local game).
 *
 * <p>Order matters on Linux: the game host (Wine / ChaosLauncher / StarCraft)
 * is killed <b>before</b> the JVM, otherwise the bot's last frames run against
 * a dead game and each one throws. The JVM is killed last, by {@code AGame.exit()}.</p>
 */
public class Exit {
    public static void handle() {
        AConsole.println("\nExit was requested manually. Cleaning up...");

        if (Env.isLocal()) {
            // Escape kills everything, the way the Windows setup did: the game
            // host (Wine / ChaosLauncher / StarCraft), the bot's own JVM under
            // Wine if one is running, and finally this JVM. Both Wine backends
            // (WINE supervisor and WINECLIENT bot) must do it - WINECLIENT does
            // not set isWine(), so it needs its own branch.
            if (Env.isWine()) {
                ProcessHelper.killWineProcesses();
                ProcessHelper.killWineClientJvm();
            }
            if (Env.isWineClient()) {
                // The client JVM is itself the process being killed, so this
                // shell-out is the practical way to take SC and the launcher
                // down from inside Wine (pkill/wineserver exist on the host).
                executeHostKillCommands();
            }
            AGame.exit();
        }
    }

    /**
     * Force-kills the whole Wine session from the bot's JVM (running under
     * Wine): the game, the launcher and the wineserver. The commands run on
     * the HOST via sh, not inside Wine - Wine passes unknown executables
     * through, and "pkill"/"wineserver" are host binaries.
     */
    private static void executeHostKillCommands() {
        String[] commands = {
            "pkill -9 -x StarCraft.exe",
            "pkill -9 Chaoslauncher",
            "wineserver -k",
        };
        for (String command : commands) {
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            } catch (Exception ignored) {
            }
        }
    }
}
