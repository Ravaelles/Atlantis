/**
 * Where the army belongs and whether it marches (02_COMBAT decision
 * layer): the defended ground, the attack ground, the posture with its
 * named reason, and the latch that keeps flips from oscillating.
 *
 * <p>New combat code lands in packages like this one - never mixed
 * into the legacy {@code atlantis.battle.*} tree (CONVENTIONS §33).
 * The value objects this package decides over ({@code AFocusPoint})
 * stay where they are referenced from; only the decisions moved.</p>
 */
package atlantis.combat.focus;
