package atlantis.keyboard.actions;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;

public class Exit {
    public static void handle() {
        AConsole.println("\nExit was requested manually. Cleaning up...");

        if (Env.isLocal()) AGame.exit();
    }
}
