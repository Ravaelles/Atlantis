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

    public static int x = 0;
    public static int y = 0;
    public static int width = 1600;
    public static int height = 1000;

    private WineWindowConfig() {
    }

    public static void applyEnvValue(String key, String value) {
        switch (key) {
            case "WINE_WINDOW_X":
                x = parseInt(value, x);
                break;
            case "WINE_WINDOW_Y":
                y = parseInt(value, y);
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

    public static String describe() {
        return "wine window " + width + "x" + height + " at (" + x + "," + y + ")";
    }
}