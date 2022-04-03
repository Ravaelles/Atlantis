package atlantis.util;

import atlantis.game.A;
import atlantis.units.AUnit;
import tests.unit.FakeUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TargetsForUnits {

    /**
     * If a Ghost ("Executor") just targeted Shuttle ("Target"), it's:  Shuttle#1 => Ghost#2
     */
    private TreeMap<AUnit, ArrayList<AUnit>> targetsToExecutors = new TreeMap<>();

    /**
     * If a Ghost ("Executor") just targeted Shuttle ("Target"), it's:  Ghost#2 => Shuttle#1
     */
    private TreeMap<AUnit, AUnit> executorsToTargets = new TreeMap<>();

    /**
     * If a Ghost just targeted Shuttle, it's:  Ghost#2 => Frames_acquired (A.now() e.g. at 491th frame)
     */
    private TreeMap<AUnit, Integer> executorsTargetAcquiredAtFrame = new TreeMap<>();

    /**
     * If a Ghost just targeted Shuttle, it's:  Shuttle#1 => Frames_acquired (A.now() e.g. at 491th frame)
     */
    private TreeMap<AUnit, Integer> targetsToAcquiredAtFrame = new TreeMap<>();

    // === Add ===========================================

    public void addTarget(AUnit target, AUnit executor) {
        ArrayList<AUnit> executors;

        // Add Executor
        if (!targetsToExecutors.containsKey(target)) {
            executors = new ArrayList<>();
        }
        else {
            executors = targetsToExecutors.get(target);
            executors.add(executor);
        }
        targetsToExecutors.put(target, executors);
        executorsToTargets.put(executor, target);

        // Add the moment when target was acquired by the Executor
        executorsTargetAcquiredAtFrame.put(executor, A.now());
        targetsToAcquiredAtFrame.put(target, A.now());
    }

    // === Get ===========================================

    public AUnit targetFor(AUnit executor) {
        return executorsToTargets.get(executor);
    }

    public Collection<AUnit> targetsAcquiredInLast(int targetMaxFramesAgo) {
        return executorsToTargets
            .values()
            .stream()
            .filter(u -> (
                (targetsToAcquiredAtFrame.get(u) + targetMaxFramesAgo >= A.now())
            ))
            .collect(Collectors.toList());
    }

    public int targetedCountInLast(AUnit target, int targetMaxFramesAgo) {
        if (!targetsToExecutors.containsKey(target)) {
            return 0;
        }

        int count = 0;
        ArrayList<AUnit> executors = targetsToExecutors.get(target);
        for (AUnit executor : executors) {
            if (executorsTargetAcquiredAtFrame.get(executor) + targetMaxFramesAgo < A.now()) {
                count++;
            }
        }

        return count;
    }

    // =========================================================

    public void print(String message) {
        System.out.println("### " + message + " ###");
        for (AUnit executor : executorsToTargets.keySet()) {
            AUnit target = executorsToTargets.get(executor);

            System.out.println("- Executor (" + executor + ") has target (" + target + ")");
        }
    }
}
