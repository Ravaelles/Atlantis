package atlantis.combat.micro.terran.bunker;

public class ShouldBuildBunkerIfOneBase {
    public static boolean shouldBuild() {
        return TerranBunker.existingOrInProduction() == 0;
    }
}
