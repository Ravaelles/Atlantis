package atlantis.debug;

public class DebugFlags {
    public static final boolean DEBUG_UNIT_COMMANDS = false;
//    public static final boolean DEBUG_UNIT_COMMANDS = true;

    public static boolean DEBUG_TARGETING = false;
//    public static boolean DEBUG_TARGETING = true;

    /**
     * Per-unit CherryVis narrative (builder issues, refusals, reassignments).
     * Const-false like the rest here: compiled away when off, so a gated
     * {@code CV.unitLog} line costs nothing in a normal game.
     */
    public static final boolean CHERRYVIS_BUILDER_ACTIONS = false;

    /**
     * Every order of every non-building unit, as CherryVis per-unit lines.
     * Same deal as above: flip for a traced run, keep off otherwise.
     */
    public static final boolean CHERRYVIS_UNIT_ORDERS = false;
}
