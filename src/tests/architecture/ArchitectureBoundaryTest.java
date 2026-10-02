package tests.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Stage B — Boundary Ratchet (see DOCS/ARCHITECTURE-CONTEXT-MAP.md and
 * _AI/REVIEW.md §16 Stage B).
 *
 * <p>These tests encode the agreed dependency directions. Today's violations are
 * <b>frozen</b> in the ArchUnit store, so this suite fails only on <b>new</b>
 * violations. Do not "fix" a red build by editing the store to add a violation;
 * fix the dependency or get it explicitly agreed.</p>
 *
 * <p>The store lives in {@code _AI/architecture/archunit-store} and is configured
 * by {@code archunit.properties} in the project root. The long-term goal
 * (Stage J) is an empty store: zero violations.</p>
 */
public class ArchitectureBoundaryTest {

    private static final JavaClasses ATLANTIS =
        new ClassFileImporter().importPackages("atlantis");

    /** core must not depend on its consumers (Combat, Production, Intelligence, Scouting, Economy). */
    @Test
    void coreMustNotDependOnOtherContexts() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage(
                "atlantis.units",
                "atlantis.units..",
                "atlantis.map.position..",
                "atlantis.decisions.."
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "atlantis.combat..",
                "atlantis.production..",
                "atlantis.information..",
                "atlantis.protoss..",
                "atlantis.terran..",
                "atlantis.map.scout..",
                "atlantis.map.base..",
                "atlantis.units.workers.."
            )
            .because("core must not depend on its consumers (REVIEW §16 Stage E)");

        FreezingArchRule.freeze(rule).check(ATLANTIS);
    }

    /** the architecture package must stay a base, not depend on feature code. */
    @Test
    void architectureMustNotDependOnFeatures() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("atlantis.architecture..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "atlantis.combat..",
                "atlantis.production..",
                "atlantis.units..",
                "atlantis.game..",
                "atlantis.util..",
                "atlantis.debug.."
            )
            .because("architecture is a base package, not a consumer (REVIEW §13)");

        FreezingArchRule.freeze(rule).check(ATLANTIS);
    }

    /** util must not depend on higher layers; it is a shared kernel, not a service. */
    @Test
    void utilMustNotDependOnHigherLayers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("atlantis.util..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "atlantis.units..",
                "atlantis.game..",
                "atlantis.map..",
                "atlantis.production..",
                "atlantis.information..",
                "atlantis.combat..",
                "atlantis.debug.."
            )
            .because("util is a shared kernel and must not point upward (REVIEW §13)");

        FreezingArchRule.freeze(rule).check(ATLANTIS);
    }

    /** Map geometry must not know about combat or production. */
    @Test
    void mapGeometryMustNotDependOnCombatOrProduction() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage(
                "atlantis.map.choke..",
                "atlantis.map.region..",
                "atlantis.map.path..",
                "atlantis.map.wall..",
                "atlantis.map.high.."
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "atlantis.combat..",
                "atlantis.production..",
                "atlantis.protoss..",
                "atlantis.terran.."
            )
            .because("Map is pure geometry (DOCS/ARCHITECTURE-CONTEXT-MAP.md §4)");

        FreezingArchRule.freeze(rule).check(ATLANTIS);
    }

    /** Scouting feeds Intelligence and Map; it must not depend on Combat or Production. */
    @Test
    void scoutingMustNotDependOnCombatOrProduction() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("atlantis.map.scout..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "atlantis.combat..",
                "atlantis.production.."
            )
            .because("Scouting → Intelligence, Map (DOCS/ARCHITECTURE-CONTEXT-MAP.md §4)");

        FreezingArchRule.freeze(rule).check(ATLANTIS);
    }

    /**
     * Intelligence must not depend on Combat or Production internals. Intelligence
     * is read by them, not the other way around.
     */
    @Test
    void intelligenceMustNotDependOnCombatOrProduction() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("atlantis.information..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "atlantis.combat..",
                "atlantis.production.."
            )
            .because("Intelligence is a source context (DOCS/ARCHITECTURE-CONTEXT-MAP.md §4)");

        FreezingArchRule.freeze(rule).check(ATLANTIS);
    }
}
