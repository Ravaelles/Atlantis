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
    public static void send(int keyCode, int keyLocation) {
        // Deliberately loud: this is how a key's REAL code is discovered when a
        // shortcut does not work (the right-Ctrl mystery was solved exactly by
        // this line - its code 3665 has no library constant and no location
        // signature anyone would guess). Remove only after wiring the key.
        System.out.println("A keyCode=" + keyCode + ", keyLocation=" + keyLocation);

        try {
            File file = file();
            file.getParentFile().mkdirs();
            Files.write(
                file.toPath(),
                (keyCode + ":" + keyLocation + "\n").getBytes(StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
        }
    }

    /** Bot side: take every key queued so far (code:location lines) and remove the file. */
    public static List<int[]> drain() {
        List<int[]> codes = new ArrayList<>();
        File file = file();
        if (!file.isFile()) return codes;

        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                try {
                    String[] parts = line.trim().split(":", 2);
                    if (parts.length == 2) {
                        codes.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
                    } else {
                        // Legacy plain-code lines from an older supervisor: run
                        // them with the standard location.
                        codes.add(new int[]{Integer.parseInt(parts[0]), 1});
                    }
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
                for (int[] key : drain()) {
                    AKeyboard.dispatchKeyCode(key[0], key[1]);
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
