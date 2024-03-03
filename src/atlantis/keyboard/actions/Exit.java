package atlantis.keyboard.actions;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;

public class Exit {
    public static void handle() {
        A.println("\nExit was requested manually. Cleaning up...");

        if (Env.isLocal()) AGame.exit();
    }
}
