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
            // Wine if one is running, and finally this JVM.
            if (Env.isWine()) {
                ProcessHelper.killWineProcesses();
                ProcessHelper.killWineClientJvm();
            }
            AGame.exit();
        }
    }
}
