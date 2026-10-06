package atlantis.config.launcher;

import atlantis.config.env.Env;

/**
 * Window geometry for the Wine virtual desktop that hosts StarCraft.
 *
 * <p>StarCraft 1.16.1 is a 1998 title: run bare it switches the X screen to a
 * low resolution and resets HiDPI scaling on exit (measured 2026-10-05, a
 * 3840x2160 desktop with 200% scaling). Running it inside a Wine virtual
 * desktop ({@code wine explorer /desktop=scgame,WxH}) keeps the game in its
 * own window and leaves the host desktop untouched.</p>
 *
 * <p>Values come from {@code bwapi-data/AI/ENV} so they can be tweaked
 * without recompiling:</p>
 * <pre>
 *   WINE_WINDOW_X=0
 *   WINE_WINDOW_Y=0
 *   WINE_WINDOW_WIDTH=1600
 *   WINE_WINDOW_HEIGHT=1000
 * </pre>
 *
 * <p>{@code WIDTH}/{@code HEIGHT} are passed to {@code wine explorer}, which
 * owns the window size. {@code X}/{@code Y} are applied afterwards with
 * {@code wmctrl -e} (Wine gives no position option); if {@code wmctrl} is not
 * installed the window simply opens wherever the window manager puts it.</p>
 */
public final class WineWindowConfig {

    /** Wine virtual-desktop name; fixed, it is an internal handle. */
    public static final String DESKTOP_NAME = "Default";

    /** Sentinels: "no position requested", so the launcher may center the window. */
    public static final int UNSET = Integer.MIN_VALUE;

    private static int x = UNSET;
    private static int y = UNSET;
    public static int width = 1600;
    public static int height = 1000;

    private WineWindowConfig() {
    }

    public static void applyEnvValue(String key, String value) {
        switch (key) {
            case "WINE_WINDOW_X":
                // Absent or empty means "not set": keep the sentinel and center.
                if (hasValue(value)) x = parseInt(value, x);
                break;
            case "WINE_WINDOW_Y":
                if (hasValue(value)) y = parseInt(value, y);
                break;
            case "WINE_WINDOW_WIDTH":
                width = parseInt(value, width);
                break;
            case "WINE_WINDOW_HEIGHT":
                height = parseInt(value, height);
                break;
            default:
                // not ours
        }
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    /** The {@code /desktop=...} argument is no longer used; kept for docs. */
    public static String desktopArgument() {
        return "/desktop=" + DESKTOP_NAME + "," + width + "x" + height;
    }

    /**
     * Command that turns on the Wine virtual desktop at our size.
     *
     * <p>Why the registry and not {@code wine explorer /desktop=}: measured
     * 2026-10-05, {@code /desktop=scgame,WxH} creates a window that Mutter
     * maximizes to the full screen (3840x2160), and neither {@code wmctrl -r}
     * nor {@code xdotool windowsize} can resize it afterwards - the window is
     * not managed by the WM. The registry desktop (this method) makes Wine
     * create a normal window of the requested size that is left alone.</p>
     *
     * <p>Wine reads the setting at wineserver start, so a running server has to
     * be killed first ({@code wineserver -k}).</p>
     */
    public static String[] enableVirtualDesktopCommands() {
        String size = width + "x" + height;
        return new String[]{
            "wineserver -k",
            "wine reg add 'HKCU\\Software\\Wine\\Explorer' /v Desktop /d " + DESKTOP_NAME + " /f",
            "wine reg add 'HKCU\\Software\\Wine\\Explorer\\Desktops' /v " + DESKTOP_NAME + " /d " + size + " /f",
        };
    }

    /**
     * Commands that place the Wine desktop window, run after the window exists.
     *
     * <p>Wine gives no option for the desktop position, so the position is set
     * with {@code wmctrl} on the X window {@code "Default - Wine desktop"}. When
     * {@code WINE_WINDOW_X}/{@code Y} are absent (the default) the window is
     * centered on the primary X screen, whose size is read with
     * {@code xdotool}.</p>
     *
     * <p>{@code wmctrl} takes gravity 0 (top-left anchor), so the window size
     * never changes - only its position.</p>
     *
     * <p>The command is a shell fragment because the window title is matched by
     * name; it uses only {@code ${...}} expansions and {@code $(( ))} arithmetic
     * so a {@code sh -c} interpreter (dash) runs it the same as bash.</p>
     */
    public static String positionWindowCommand() {
        String target = DESKTOP_NAME + " - Wine desktop";
        StringBuilder script = new StringBuilder();

        // Single-quote the title for an inner eval, and escape any single quote
        // it might contain (it contains none today; the escape is cheap).
        String quotedTarget = "'" + target.replace("'", "'\\''") + "'";
        script.append("eval \"set -- ").append(quotedTarget).append("\" || exit 0; ");
        script.append("test -n \"${DISPLAY:-}\" || exit 0; ");

        if (x != UNSET && y != UNSET) {
            script.append("command -v wmctrl >/dev/null 2>&1 || exit 0; ");
            script.append("wmctrl -x -r \"$1\" -e 0,").append(x).append(',').append(y).append(",-1,-1");
            return script.toString();
        }

        // Centering needs the screen size. The fallback 1920x1080 only applies
        // when xdotool is missing, and wrong centering is cosmetic, never fatal.
        script.append("command -v wmctrl >/dev/null 2>&1 || exit 0; ");
        script.append("SR=$(xdotool getdisplaygeometry 2>/dev/null || echo '1920 1080'); ");
        script.append("SW=${SR%% *}; SH=${SR#* }; ");
        script.append("wmctrl -x -r \"$1\" -e 0,$(( (SW-").append(width).append(")/2 ))");
        script.append(",$(( (SH-").append(height).append(")/2 ))-1,-1");
        return script.toString();
    }

    public static String describe() {
        if (x == UNSET || y == UNSET) {
            return "wine window " + width + "x" + height + " (centered)";
        }
        return "wine window " + width + "x" + height + " at (" + x + "," + y + ")";
    }
}