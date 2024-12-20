package atlantis.map.base;

import jbweb.Stations;

import java.util.List;
import java.util.stream.Collectors;

public class AllBaseLocations {
    public static List<ABaseLocation> get() {
        return Stations.allBases()
            .stream()
            .map(base -> ABaseLocation.create(base.getBWEMBase()))
            .collect(Collectors.toList());
    }
}
