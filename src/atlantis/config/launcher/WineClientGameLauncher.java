package atlantis.config.launcher;

import atlantis.config.ActiveMap;
import atlantis.keyboard.AKeyboard;
import atlantis.keyboard.KeyRelay;
import main.Main;

/**
 * Wine-JVM backend: the bot's own JVM runs <b>under Wine</b>, next to
 * StarCraft, so it can see BWAPI's Windows shared memory.
 *
 * <p>
 * Why this exists (measured 2026-10-06): BWAPI injected into StarCraft
 * under Wine creates Windows named sections through wineserver - nothing
 * appears in {@code /dev/shm}. A native Linux JVM client therefore never sees
 * the game table and loops on "Game table mapping not found" forever, while
 * a JVM running under Wine reaches the same table fine (measured: the client
 * got to the table-waiting state on its first run).
 * </p>
 *
 * <p>
 * Unlike {@link UnixChaosGameLauncher} this launcher deliberately does
 * <b>not</b> start or kill anything: the game is brought up by
 * {@code scripts/run-wine-game.sh} (or a previous bot start on Linux), the
 * JVM here is already a Windows process, and pkill/wmctrl/wine do not exist
 * for it. It only picks the map and waits for {@code BWClient.startGame()} to
 * attach - the same shape as {@code OpenBWGameLauncher}.
 * </p>
 *
 * <p>
 * Selected with {@code GAME_LAUNCHER=WINECLIENT} in
 * {@code bwapi-data/AI/ENV}, which the Wine-side JVM reads from the repo's
 * {@code bwapi-data} (run with the repo as the working directory).
 * </p>
 */
public class WineClientGameLauncher implements GameLauncher {

    @Override
    public void launch(String[] args) {
        ActiveMap.specifyMap(Main.defineMapToUse(args));

        System.out.println("===============================================");
        System.out.println("[Atlantis] Backend: Java under Wine (client only).");
        System.out.println("[Atlantis] Map: " + ActiveMap.name());
        System.out.println("[Atlantis] Not starting the game - it must already");
        System.out.println("[Atlantis] run under Wine (run-wine-game.sh or a");
        System.out.println("[Atlantis] previous Linux-side bot start does that).");
        System.out.println("[Atlantis] Waiting for the BWAPI game table...");
        System.out.println("===============================================");

        // This JVM has the game, so it owns the speed/camera shortcuts. Its own
        // hook under Wine only sees keys typed into Wine windows, so the Linux
        // supervisor forwards key codes through KeyRelay - drain them and
        // execute. AKeyboard no longer kills the JVM when its hook fails.
        AKeyboard.listenForKeyEvents();
        KeyRelay.startDraining();
    }
}
