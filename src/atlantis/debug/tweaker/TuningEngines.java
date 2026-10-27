package atlantis.debug.tweaker;

import java.util.*;

/**
 * The engines the tuner loop can address, by CLI name. The loop asks
 * here instead of hardcoding paths: adding an engine is a new class,
 * not a new branch in the scripts.
 */
public final class TuningEngines {

    private TuningEngines() {
    }

    public static List<TuningEngine> all() {
        return Collections.unmodifiableList(Arrays.asList(
            new WineEngine(), new OpenBWEngine(), new SimEngine()));
    }

    /**
     * The engine for a CLI name, or empty when nothing matches.
     */
    public static java.util.Optional<TuningEngine> fromName(String name) {
        if (name == null) return java.util.Optional.empty();

        for (TuningEngine engine : all()) {
            if (engine.name().equalsIgnoreCase(name.trim())) return java.util.Optional.of(engine);
        }
        return java.util.Optional.empty();
    }

    /**
     * name -> batch script, for the loop. Unknown names are absent, never null.
     */
    public static Map<String, String> batchScripts() {
        Map<String, String> scripts = new LinkedHashMap<>();
        for (TuningEngine engine : all()) scripts.put(engine.name(), engine.batchScript());

        return scripts;
    }
}
