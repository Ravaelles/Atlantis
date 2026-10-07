package atlantis.config.env;

/**
 * How production-v2 (M4 of _AI/redesign/01_PRODUCTION.md) participates in the
 * game, as read from {@code PRODUCTION_V2} in the ENV file.
 *
 * <p>
 * The three states are the cutover path of the redesign, in order: the legacy
 * queue plays alone ({@link #OFF}), the v2 engine plans alongside it from the
 * same game state and only logs what it would order ({@link #DRY_RUN}), and
 * then
 * v2 issues the orders ({@link #LIVE}). A flag rather than a code edit is what
 * makes the middle state possible to observe in a real game - the step that
 * decides whether the cutover is safe.
 * </p>
 */
public enum ProductionV2Mode {

    /** Legacy queue only. The default, so a missing key changes nothing. */
    OFF,

    /** v2 plans every frame and logs the comparison; no command leaves. */
    DRY_RUN,

    /** v2 issues the commands; the legacy queue is not asked to produce. */
    LIVE;

    /**
     * Parses the ENV value. Anything unrecognised is {@link #OFF} on purpose:
     * a typo must not silently enable the new engine in a tournament game.
     */
    public static ProductionV2Mode parse(String value) {
        if (value == null)
            return OFF;

        String normalized = value.trim().toUpperCase();
        if (normalized.equals("DRY_RUN") || normalized.equals("DRYRUN"))
            return DRY_RUN;
        if (normalized.equals("TRUE") || normalized.equals("LIVE") || normalized.equals("ON"))
            return LIVE;
        return OFF;
    }

    public boolean isEnabled() {
        return this != OFF;
    }

    public boolean isDryRun() {
        return this == DRY_RUN;
    }

    public boolean isLive() {
        return this == LIVE;
    }
}
