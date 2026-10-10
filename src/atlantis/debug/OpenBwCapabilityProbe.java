package atlantis.debug;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;

/**
 * One-shot OpenBW capability survey: asks the engine every query the bot
 * depends
 * on, <b>from a Probe's point of view</b>, and prints the answers.
 *
 * <p>
 * It exists because OpenBW answered a question we never thought to ask and the
 * discovery cost days: {@code Unit.build} was refused on a tile that every
 * other
 * query called fine, and the culprit was
 * {@code Templates::canBuildHere}'s final check,
 * {@code builder->hasPath(siteCentre)}, which returns <b>false for adjacent,
 * walkable tiles</b> on this headless engine ({@code _AI/CHALLENGES/Bwapi.md},
 * NEXT #47/#48). Each such surprise was found one at a time, live, in a game -
 * this probe turns the next one into a table instead of an investigation.
 * </p>
 *
 * <p>
 * <b>What it does not do:</b> it never issues an order that changes the game.
 * It
 * only <i>reads</i> engine answers (plus the {@code canBuild}/{@code hasPath}
 * predicates, which are pure queries). That keeps it usable on a live run
 * without
 * disturbing what it measures - the lesson from the scenario probes that were
 * actuators rather than sensors ({@code _AI/NOTES.md}).
 * </p>
 *
 * <p>
 * Enabled with {@code OPENBW_PROBE=1} in ENV; the flag is diagnostic only and
 * is
 * never set in a real game or a tournament run. It runs on a few early frames
 * so
 * the run stays inside the 20-second single-test budget (CONVENTIONS §17), and
 * it
 * prints a stable, greppable block: every line starts with
 * {@code OPENBW_PROBE}.
 * </p>
 *
 * <p>
 * The results as of 2026-10-10 are in {@code _AI/CHALLENGES/OpenBW-API.md} -
 * read
 * that file before trusting any of these queries on this engine.
 * </p>
 */
public final class OpenBwCapabilityProbe {

    /**
     * Lines carry this prefix so one grep pulls the whole survey out of the log.
     */
    public static final String PREFIX = "OPENBW_PROBE";

    /**
     * Frames to run on. A few, so several positions are sampled as the Probe
     * moves, but few enough that the probe cannot dominate a short run.
     */
    private static final int SAMPLE_FRAMES = 6;
    private static final int SAMPLE_EVERY_NTH_FRAME = 20;

    private static int samplesTaken = 0;

    private OpenBwCapabilityProbe() {
    }

    /**
     * Per-frame entry point, called from {@code DebugCommander}. Cheap and inert
     * unless the ENV flag is set, and it stops itself after a handful of samples.
     */
    public static void update() {
        if (!atlantis.config.env.Env.openBwProbe())
            return;
        if (samplesTaken >= SAMPLE_FRAMES)
            return;
        if (!A.everyNthGameFrame(SAMPLE_EVERY_NTH_FRAME))
            return;

        samplesTaken++;
        survey();
    }

    private static void survey() {
        Game game = atlantis.Atlantis.game();
        if (game == null)
            return;

        AUnit probe = Select.ourWorkers().first();
        if (probe == null) {
            line("frame=" + A.now() + " no Probe yet - cannot survey from a builder's point of view");
            return;
        }

        line("frame=" + A.now() + " engine=" + (atlantis.config.env.Env.isOpenBW() ? "OpenBW" : "other")
                + " race=" + atlantis.config.AtlantisRaceConfig.MY_RACE
                + " probe=#" + probe.id() + "@" + probe.tx() + "," + probe.ty());

        surveyMapQueries(game, probe);
        surveyPathQueries(game, probe);
        surveyBuildQueries(game, probe);
    }

    // ---------------------------------------------------------------------------
    // The map questions: walkable / buildable / explored / visible
    // ---------------------------------------------------------------------------

    private static void surveyMapQueries(Game game, AUnit probe) {
        // Walk the tiles around the Probe: that is the neighbourhood a placement
        // decision is actually made in, so a "broken" answer there matters far more
        // than one at map centre.
        int badWalk = 0;
        int badBuild = 0;
        int badExplored = 0;
        int tiles = 0;
        StringBuilder sample = new StringBuilder();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int tx = probe.tx() + dx;
                int ty = probe.ty() + dy;
                if (tx < 0 || ty < 0)
                    continue;

                tiles++;

                // Tile-coordinate queries. isWalkable takes a WALK position
                // (8x8 px) - passing tiles here silently reads the wrong cell, which
                // cost a false lead once (see NEXT #48), so it is converted
                // explicitly and labelled.
                boolean walkable = game.isWalkable(new TilePosition(tx, ty).toWalkPosition());
                boolean buildable = game.isBuildable(tx, ty);
                boolean buildableInc = game.isBuildable(tx, ty, true);
                boolean explored = game.isExplored(tx, ty);
                boolean visible = game.isVisible(tx, ty);
                APosition p = APosition.create(tx, ty);

                if (!walkable)
                    badWalk++;
                if (!buildable)
                    badBuild++;
                if (!explored)
                    badExplored++;

                if (tiles <= 6) {
                    sample.append(" (").append(tx).append(',').append(ty).append(")")
                            .append("w=").append(walkable ? 1 : 0)
                            .append("b=").append(buildable ? 1 : 0)
                            .append("bi=").append(buildableInc ? 1 : 0)
                            .append("e=").append(explored ? 1 : 0)
                            .append("v=").append(visible ? 1 : 0)
                            .append("apiW=").append(p.isWalkable() ? 1 : 0)
                            .append("apiB=").append(p.isBuildableNotIncludingBuildings() ? 1 : 0);
                }
            }
        }

        line("MAP tiles=" + tiles
                + " notWalkable=" + badWalk
                + " notBuildable=" + badBuild
                + " notExplored=" + badExplored
                + " || rounds: visible=" + game.isVisible(probe.tx(), probe.ty())
                + " frame=" + game.getFrameCount());
        line("MAP sample" + sample);

        // The two path questions, and the tile-vs-walk coordinate trap spelled out.
        line("MAP mapSize=" + game.mapWidth() + "x" + game.mapHeight()
                + " tileSize=" + new TilePosition(0, 0).toWalkPosition().getX());
    }

    // ---------------------------------------------------------------------------
    // The path question: the one that broke building
    // ---------------------------------------------------------------------------

    private static void surveyPathQueries(Game game, AUnit probe) {
        Position from = probe.u().getPosition();

        // Pairs at increasing distance, all on the open ground around the Probe.
        // The measured OpenBW failure was "no path" between adjacent tiles, so the
        // small distances are the interesting ones.
        int[] distancesPx = { 32, 64, 128, 256 };
        StringBuilder sb = new StringBuilder();

        for (int px : distancesPx) {
            Position to = new Position(from.getX() + px, from.getY());
            boolean gamePath = game.hasPath(from, to);
            boolean unitPath = probe.u().hasPath(to);
            sb.append(" d").append(px).append("px:");
            sb.append("game=").append(gamePath ? 1 : 0);
            sb.append(",unit=").append(unitPath ? 1 : 0);
        }

        line("PATH from=" + from.getX() + "," + from.getY() + sb);

            // And the diagonal/self cases, where a sane engine answers true.
            Position self = new Position(from.getX(), from.getY());
            line("PATH self: game=" + (game.hasPath(from, self) ? 1 : 0)
                + " unit=" + (probe.u().hasPath(self) ? 1 : 0));

            // WHY. `hasPath` is `getRegionAt(a)->groupID == getRegionAt(b)->groupID`
            // (BWAPI Game::hasPath -> RegionImpl::getRegionGroupID -> OpenBW
            // Regions::group_index), so name the two regions and their groups rather
            // than inferring them from a boolean.
                line("REGION " + describeRegion(game, from) + " || " + describeRegion(game, to32(from)));

                // The decisive question: does the engine have ANY regions at all? If every
                // region lookup returns nothing, regions were never built in this run, and
                // that - not our code - is why hasPath is false. Count what comes back over
                // the whole map rather than trusting one sample.
                int nonNull = 0;
                int distinctGroups = 0;
                java.util.Set<Integer> groups = new java.util.TreeSet<>();
                int step = 8;
                int probes = 0;
                for (int tx = 0; tx < game.mapWidth(); tx += step) {
                    for (int ty = 0; ty < game.mapHeight(); ty += step) {
                        probes++;
                        try {
                            bwapi.Region r = game.getRegionAt(new Position(tx * 32, ty * 32));
                            if (r != null) {
                                nonNull++;
                                groups.add(r.regionGroupID);
                            }
                        } catch (Throwable ignored) {
                            // treated as null
                        }
                    }
                }
                distinctGroups = groups.size();
                line("REGIONS probes=" + probes + " nonNull=" + nonNull
                    + " distinctGroups=" + distinctGroups
                    + " groups=" + groups);
            }

        private static Position to32(Position p) {
            return new Position(p.getX() + 32, p.getY());
        }

        private static String describeRegion(Game game, Position p) {
            try {
                bwapi.Region r = game.getRegionAt(p);
                if (r == null) return "p=" + p.getX() + "," + p.getY() + " region=null";
                return "p=" + p.getX() + "," + p.getY()
                    + " regionId=" + r.getID()
                    + " groupId=" + r.regionGroupID
                    + " accessible=" + r.isAccessible()
                    + " higherGround=" + r.isHigherGround()
                    + " neighbors=" + (r.neighbours == null ? -1 : r.neighbours.size())
                    + " bounds=[" + r.boundsLeft + "," + r.boundsTop + "," + r.boundsRight + "," + r.boundsBottom + "]";
            } catch (Throwable t) {
                return "p=" + p.getX() + "," + p.getY() + " region=EXCEPTION(" + t + ")";
            }
        }

    // ---------------------------------------------------------------------------
    // The build question: canBuildHere and the preconditions it is built from
    // ---------------------------------------------------------------------------

    private static void surveyBuildQueries(Game game, AUnit probe) {
        // Ask about the tiles the Probe is standing on and just beside it: that is
        // where a builder actually places things.
        int[][] offsets = {
                { 0, 0 }, { 2, 0 }, { 0, 2 }, { 2, 2 }, { -2, 0 }, { 0, -2 }, { 2, -2 }, { -2, 2 }
        };

        StringBuilder sb = new StringBuilder();
        for (int[] off : offsets) {
            int tx = probe.tx() + off[0];
            int ty = probe.ty() + off[1];
            if (tx < 0 || ty < 0)
                continue;

            TilePosition tile = new TilePosition(tx, ty);
            boolean canBuildHere = game.canBuildHere(tile, AUnitType.Protoss_Pylon.ut(), probe.u(), true);
            boolean canIssue = probe.u().canBuild(AUnitType.Protoss_Pylon.ut(), tile);

            // The site centre Templates::canBuildHere uses for its path check.
            Position siteCentre = new Position(
                    tile.getX() * 32 + AUnitType.Protoss_Pylon.getTilesWidth() * 16,
                    tile.getY() * 32 + AUnitType.Protoss_Pylon.getTilesHeights() * 16);
            boolean sitePath = probe.u().hasPath(siteCentre);

            sb.append(" [").append(tx).append(',').append(ty).append("]")
                    .append("cBH=").append(canBuildHere ? 1 : 0)
                    .append(",canIssue=").append(canIssue ? 1 : 0)
                    .append(",sitePath=").append(sitePath ? 1 : 0);
        }

        line("BUILD pylonAroundProbe" + sb);
        line("BUILD note: Templates::canBuildHere = terrain + explored + (explicitly NOT occupancy)"
                + " + builder->hasPath(siteCentre); a false sitePath alone refuses every tile");
    }

    private static void line(String message) {
        // System.out, not ErrorLog: the survey is a report, not an error, and
        // ErrorLog would rate-limit parts of it away.
        System.out.println(PREFIX + " " + message);
    }
}
