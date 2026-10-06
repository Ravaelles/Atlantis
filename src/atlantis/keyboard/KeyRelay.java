package atlantis.keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The key channel between the two JVMs of the Wine setup.
 *
 * <p>Why this exists (measured 2026-10-06): the bot's JVM runs under Wine with
 * {@code -Dos.name=Windows 10}, so JNativeHook loads the Windows native - and
 * Wine only delivers keyboard events to Wine applications. Keys pressed
 * anywhere else (the IDE, a browser) never reach it. The Linux supervisor JVM
 * sees every key but has no BWAPI. So the supervisor captures keys and writes
 * their codes to a file; the bot drains the file and executes them.</p>
 *
 * <p>The file is a plain append log, drained by deleting: the supervisor
 * appends key codes, the bot reads everything and removes the file. Nothing
 * is left behind on exit.</p>
 */
public class KeyRelay {

    /** Absolute first (the workspace root is stable, see CONVENTIONS section 8), CWD fallback second. */
    private static final String[] CANDIDATE_PATHS = {
        "/sc-ai/Atlantis/out/wine/keys.txt",
        "out/wine/keys.txt",
    };

    private KeyRelay() {
    }

    private static File file() {
        for (String candidate : CANDIDATE_PATHS) {
            File file = new File(candidate);
            if (file.getParentFile().isDirectory()) {
                return file;
            }
        }

        // Neither known root exists (unusual: started from a foreign directory).
        // Create under the CWD so the channel still works.
        File fallback = new File("out/wine/keys.txt");
        fallback.getParentFile().mkdirs();
        return fallback;
    }

    /** Supervisor side: record a key code for the bot to execute. Never throws. */
    public static void send(int keyCode) {
        try {
            File file = file();
            file.getParentFile().mkdirs();
            Files.write(
                file.toPath(),
                (keyCode + "\n").getBytes(StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
        }
    }

    /** Bot side: take every key code queued so far and remove the file. */
    public static List<Integer> drain() {
        List<Integer> codes = new ArrayList<>();
        File file = file();
        if (!file.isFile()) return codes;

        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                try {
                    codes.add(Integer.parseInt(line.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            Files.delete(file.toPath());
        } catch (IOException ignored) {
        }

        return codes;
    }

    /**
     * Bot side: start a daemon thread that executes drained keys on the real
     * handler. Polling every 100 ms is plenty for human keystrokes.
     */
    public static void startDraining() {
        Thread thread = new Thread(() -> {
            while (true) {
                for (int keyCode : drain()) {
                    AKeyboard.dispatchKeyCode(keyCode);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "key-relay-drain");
        thread.setDaemon(true);
        thread.start();
    }
}
