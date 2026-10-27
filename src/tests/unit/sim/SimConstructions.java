package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sim execution of the legacy construction pipeline: builder travel,
 * warp start (minerals leave the bank here, like the engine), warp
 * progress, completion, plus facility training queues and research
 * timers.
 *
 * <p>The bot only <em>decides</em> (a {@code Construction} request, a
 * train order on a facility). Everything the engine used to do after
 * that - walking, deducting, progressing, spawning, completing - lives
 * here, against the same objects the bot reads ({@code Construction},
 * {@code Select} lists, stub training queues). No production code is
 * touched: this is the sim side of the strangler seam.</p>
 */
public final class SimConstructions {

    /**
     * Arrival radius: the builder warps from next to the site.
     */
    static final double ARRIVE_TILES = 2.0;
    private final Map<Construction, Site> sites = new HashMap<>();

    /** Frames the sim first saw a construction, per building type name. */
    private final Map<String, java.util.List<Integer>> starts = new HashMap<>();
    private final Map<Integer, TrainingJob> training = new HashMap<>();
    private final Map<Integer, ResearchJob> research = new HashMap<>();

    /**
     * Training already running on this facility (engine dedup mirror).
     */
    public boolean isTraining(int facilityId) {
        return training.containsKey(facilityId);
    }

    public boolean isResearching(int facilityId) {
        return research.containsKey(facilityId);
    }

    public void startTraining(int facilityId, AUnitType type) {
        training.put(facilityId, new TrainingJob(type, type.totalTrainTime()));
    }

    public void startResearch(int facilityId, boolean isUpgrade, String name, int frames) {
        research.put(facilityId, new ResearchJob(isUpgrade, name, frames));
    }

    /**
     * Advances every construction, training queue and research timer by
     * one frame. New requests are picked up, validated, walked, warped
     * and completed; the driver owns movement, banking and spawning
     * through the frame context.
     */
    public void advance(SimFrame frame) {
        for (Construction construction : new ArrayList<>(ConstructionRequests.constructions)) {
            advanceConstruction(frame, construction);
        }
        advanceTraining(frame);
        advanceResearch(frame);
    }

    /** Start frames observed so far, per building type name. */
    public Map<String, java.util.List<Integer>> starts() {
        Map<String, java.util.List<Integer>> copy = new HashMap<>();
        for (Map.Entry<String, java.util.List<Integer>> e : starts.entrySet()) {
            copy.put(e.getKey(), new java.util.ArrayList<>(e.getValue()));
        }

        return copy;
    }

    public boolean hasWork() {
        if (!training.isEmpty() || !research.isEmpty()) return true;

        for (Construction construction : ConstructionRequests.constructions) {
            if (construction != null && !construction.status().isTerminal()) return true;
        }

        return false;
    }

    private void advanceConstruction(SimFrame frame, Construction construction) {
        if (construction == null || construction.buildingType() == null) return;

        if (construction.status().isTerminal()) {
            sites.remove(construction);

            return;
        }
        Site site = sites.get(construction);
        if (site == null) {
            site = new Site();
            sites.put(construction, site);
            if (construction.buildingType() != null) {
                starts.computeIfAbsent(construction.buildingType().name(),
                    k -> new java.util.ArrayList<Integer>()).add(frame.frame());
            }
        }

        if (!ensureBuilder(frame, construction, site)) return;
        if (!validateSite(frame, construction, site)) return;

        if (!arrived(site, construction)) {
            frame.driver().moveBuilderToward(frame, site.builder, construction.buildPosition());
            if (construction.status() != ConstructionOrderStatus.STARTING) {
                construction.setStatus(ConstructionOrderStatus.STARTING);
            }

            return;
        }

        if (construction.status() != ConstructionOrderStatus.IN_PROGRESS) {
            AUnitType type = construction.buildingType();
            if (!frame.bank().canAfford(type.mineralPrice(), type.gasPrice())) return;
            // Proximity finish, like the live game: a site covered by a
            // standing building cancels instead of warping - the request
            // was legal when placed, the engine kills it at warp start.
            if (coveredByStanding(frame, type, construction)) {
                frame.driver().logCancel(construction.buildingType(), construction.buildPosition(),
                    "proximity finish: standing building covers the site");
                frame.driver().freeBuilder(site.builder);
                ConstructionRequests.constructions.remove(construction);
                sites.remove(construction);

                return;
            }
            frame.bank().spend(type.mineralPrice(), type.gasPrice(), "warp " + type.name());
            site.warpFramesLeft = type.totalTrainTime();
            site.warpUnit = frame.driver().spawnWarpBuilding(type, construction.buildPosition());
            site.warpUnit.simRemainingBuildTime = site.warpFramesLeft;
            construction.setBuildingUnit(site.warpUnit);
            // Links the warp first: setStatus(IN_PROGRESS) detaches the
            // builder for Protoss (the probe leaves, like the engine).
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            frame.driver().freeBuilder(site.builder);
            site.builder = null;
            frame.driver().logWarp(construction.buildingType(), construction.buildPosition());
        }

        site.warpFramesLeft--;
        if (site.warpUnit != null) {
            site.warpUnit.simRemainingBuildTime = Math.max(0, site.warpFramesLeft);
        }
        if (site.warpFramesLeft > 0) return;

        if (site.warpUnit != null) site.warpUnit.setCompleted(true);
        construction.setStatus(ConstructionOrderStatus.FINISHED);
        sites.remove(construction);
        frame.driver().onBuildingCompleted(construction.buildingType());
    }

    private boolean ensureBuilder(SimFrame frame, Construction construction, Site site) {
        if (site.builder != null && site.builder.isAlive()) {
            construction.setBuilder(site.builder);

            return true;
        }
        FakeUnit builder = frame.driver().assignBuilder(construction.buildPosition());
        if (builder == null) return false;

        site.builder = builder;
        construction.setBuilder(builder);

        return true;
    }

    /**
     * Placement, reachability, psi and footprint are validated once, at
     * request time. Funding is NOT: the builder walks while the bank
     * fills, exactly like the live game, and warp start re-checks it.
     */
    private boolean validateSite(SimFrame frame, Construction construction, Site site) {
        if (site.validated) return true;

        site.validated = true;

        AUnitType type = construction.buildingType();
        APosition tile = construction.buildPosition();
        SimOrder verdict = SimOrderValidator.validateBuild(
            frame.ground(), site.builder, type, tile.tx(), tile.ty(),
            frame.bank(), frame.driver().completedBuildings(), false);
        if (verdict.accepted()) return true;

        frame.driver().logRejected(verdict);
        frame.driver().freeBuilder(site.builder);
        ConstructionRequests.constructions.remove(construction);
        sites.remove(construction);

        return false;
    }

    // =========================================================

    private boolean coveredByStanding(SimFrame frame, AUnitType type, Construction construction) {
        APosition tile = construction.buildPosition();
        if (tile == null) return false;

        for (FakeUnit other : frame.driver().completedBuildings()) {
            if (other != null && other.isAlive()
                && SimGround.footprintsOverlap(type, tile.tx(), tile.ty(), other)) {
                return true;
            }
        }

        return false;
    }

    private boolean arrived(Site site, Construction construction) {
        if (site.builder == null || construction.buildPosition() == null) return false;

        try {
            return site.builder.position().distTo(construction.buildPosition()) <= ARRIVE_TILES;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void advanceTraining(SimFrame frame) {
        for (Map.Entry<Integer, TrainingJob> entry : new ArrayList<>(training.entrySet())) {
            TrainingJob job = entry.getValue();
            job.framesLeft--;
            FakeUnit facility = frame.driver().unitById(entry.getKey());
            if (facility != null) {
                facility.setTrainingQueue(Collections.singletonList(job.type));
            }
            if (job.framesLeft > 0) continue;
            training.remove(entry.getKey());
            frame.driver().completeTraining(entry.getKey(), job.type);
        }
    }

    private void advanceResearch(SimFrame frame) {
        for (Map.Entry<Integer, ResearchJob> entry : new ArrayList<>(research.entrySet())) {
            ResearchJob job = entry.getValue();
            job.framesLeft--;
            if (job.framesLeft > 0) continue;
            research.remove(entry.getKey());
            frame.driver().completeResearch(entry.getKey(), job.isUpgrade, job.name);
        }
    }

    private static final class Site {
        FakeUnit builder;
        boolean validated;
        int warpFramesLeft;
        FakeUnit warpUnit;
    }

    private static final class TrainingJob {
        final AUnitType type;
        int framesLeft;

        TrainingJob(AUnitType type, int framesLeft) {
            this.type = type;
            this.framesLeft = framesLeft;
        }
    }

    private static final class ResearchJob {
        final boolean isUpgrade;
        final String name;
        int framesLeft;

        ResearchJob(boolean isUpgrade, String name, int framesLeft) {
            this.isUpgrade = isUpgrade;
            this.name = name;
            this.framesLeft = framesLeft;
        }
    }
}
