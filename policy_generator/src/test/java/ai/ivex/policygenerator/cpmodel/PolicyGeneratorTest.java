package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

/** @author Hoang Tung Dinh */
class PolicyGeneratorTest {

	private static final int MAX_PLAN_LENGTH = 3;
	private static final ImmutableSet<ActionSpec> ACTION_SPECS = ImmutableSet.of(
			ActionSpec.create("take_off", 1,
					new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
							new boolean[] { true, false }),
					new ArrayList<Object2BooleanMap<String>>(),
					(model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "on_the_ground"),
					(model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying"),
					new ArrayList<Effect>()),
			ActionSpec.create("land", 1,
					new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
							new boolean[] { true, false }),
					new ArrayList<Object2BooleanMap<String>>(),
					(model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying"),
					(model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "on_the_ground"),
					new ArrayList<Effect>()),
			ActionSpec.create("go_to_waypoint", 1,
					new Object2BooleanOpenHashMap<>(
							new String[] { "S_flying", "S_destination" }, new boolean[] { false, true }),
					new ArrayList<Object2BooleanMap<String>>(),
					(model, stateVector) -> model.and(stateVector.getHasValueConstraint("S_destination", "not_reached"),
							stateVector.getHasValueConstraint("S_flying", "flying")),
					(model, stateVector) -> stateVector.getHasValueConstraint("S_destination", "reached"),
					new ArrayList<Effect>()),
			ActionSpec.create("turn_on_led_light", 1,
					new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
							new boolean[] { false, false }),
					new ArrayList<Object2BooleanMap<String>>(), (model, stateVector) -> model.trueConstraint(),
					(model, stateVector) -> model.trueConstraint(), new ArrayList<Effect>()));
	private static final ImmutableSet<StateSpec> STATE_SPECS = ImmutableSet.of(
			StateSpec.create("S_flying", ImmutableSet.of("on_the_ground", "flying")),
			StateSpec.create("S_destination", ImmutableSet.of("not_reached", "reached")));
	private static final ImmutableSet<MutuallyExclusiveActions> MUTUALLY_EXCLUSIVE_ACTIONS = ImmutableSet
			.of(MutuallyExclusiveActions.create(ImmutableSet.of("take_off", "go_to_waypoint", "land")));
	private static final ImmutableSet<ReactionRule> REACTION_RULES = ImmutableSet
			.of((model, stateVector, planStep) -> model.ifThen(planStep.getExecutingConstraint("go_to_waypoint"),
					planStep.getExecutingConstraint("turn_on_led_light")));
	private static final ImmutableSet<StateRule> STATE_RULES = ImmutableSet.of(); // State Rules to be tested
																					// (28/04/2022)
	private static final Goals GOALS = (model, initialStateValue, initialStateVector, firstPlanStep,
			goalStateVector) -> {
		goalStateVector.getHasValueConstraint("S_flying", "on_the_ground").post();
		goalStateVector.getHasValueConstraint("S_destination", "reached").post();
	};

	@Test
	void testPolicyGenerator() {
		final PolicyGenerator policyGenerator = PolicyGenerator
				.create(Specification.create(MAX_PLAN_LENGTH, ACTION_SPECS, STATE_SPECS, MUTUALLY_EXCLUSIVE_ACTIONS,
						REACTION_RULES, STATE_RULES, ImmutableSet.of(), GOALS));

		final PolicyGenerator.Result result = policyGenerator.generatePolicy();

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getPolicy().get().getMappings()).containsExactlyInAnyOrder(
				Mapping.create(ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "not_reached"),
						ImmutableSet.of("take_off")),
				Mapping.create(ImmutableMap.of("S_flying", "flying", "S_destination", "not_reached"),
						ImmutableSet.of("go_to_waypoint", "turn_on_led_light")),
				Mapping.create(ImmutableMap.of("S_flying", "flying", "S_destination", "reached"),
						ImmutableSet.of("land")),
				Mapping.create(ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "reached"),
						ImmutableSet.of()));
	}

	@Test
	void testWithAssumptions() {
		for (int i = 0; i < 100; i++) {
			final PolicyGenerator policyGenerator = PolicyGenerator.create(Specification.create(MAX_PLAN_LENGTH,
					ACTION_SPECS, STATE_SPECS, MUTUALLY_EXCLUSIVE_ACTIONS, REACTION_RULES, STATE_RULES,
					ImmutableSet
							.of((model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying").post()),
					(model, initialStateValue, initialStateVector, firstPlanStep, goalStateVector) -> {
					}));

			final PolicyGenerator.Result result = policyGenerator.generatePolicy();

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.getPolicy().get().getMappings()).containsExactlyInAnyOrder(
					Mapping.create(ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "not_reached"),
							ImmutableSet.of()),
					Mapping.create(ImmutableMap.of("S_flying", "flying", "S_destination", "not_reached"),
							ImmutableSet.of()),
					Mapping.create(ImmutableMap.of("S_flying", "flying", "S_destination", "reached"),
							ImmutableSet.of()),
					Mapping.create(ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "reached"),
							ImmutableSet.of()));
		}
	}

	@Test
	void testFailureCase() {
		final PolicyGenerator policyGenerator = PolicyGenerator
				.create(Specification.create(MAX_PLAN_LENGTH, ACTION_SPECS, STATE_SPECS, MUTUALLY_EXCLUSIVE_ACTIONS,
						REACTION_RULES, STATE_RULES, ImmutableSet.of((model, stateVector) -> stateVector
								.getHasValueConstraint("S_destination", "not_reached").post()),
						GOALS));

		final PolicyGenerator.Result result = policyGenerator.generatePolicy();

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getPolicy()).isEmpty();
		assertThat(result.getFailedState()).isNotEmpty();
	}

	@Test
	void testLoopBug() {
		final PolicyGenerator policyGenerator = PolicyGenerator
				.create(Specification.create(MAX_PLAN_LENGTH,
						ImmutableSet.of(
								ActionSpec.create("take_off", 1,
										new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
												new boolean[] { true, false }),
										new ArrayList<Object2BooleanMap<String>>(),
										(model, stateVector) -> stateVector.getHasValueConstraint("S_flying",
												"on_the_ground"),
										(model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying"),
										new ArrayList<Effect>()),
								ActionSpec.create("land", 1,
										new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
												new boolean[] { true, false }),
										new ArrayList<Object2BooleanMap<String>>(),
										(model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying"),
										(model, stateVector) -> stateVector.getHasValueConstraint("S_flying",
												"on_the_ground"),
										new ArrayList<Effect>()),
								ActionSpec.create("go_to_waypoint", 0,
										new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
												new boolean[] { false, true }),
										new ArrayList<Object2BooleanMap<String>>(),
										(model, stateVector) -> model.and(
												stateVector.getHasValueConstraint("S_destination", "not_reached"),
												stateVector.getHasValueConstraint("S_flying", "flying")),
										(model, stateVector) -> stateVector.getHasValueConstraint("S_destination",
												"reached"),
										new ArrayList<Effect>()),
								ActionSpec.create("turn_on_led_light", 0,
										new Object2BooleanOpenHashMap<>(new String[] { "S_flying", "S_destination" },
												new boolean[] { false, false }),
										new ArrayList<Object2BooleanMap<String>>(),
										(model, stateVector) -> model.trueConstraint(),
										(model, stateVector) -> model.trueConstraint(), new ArrayList<Effect>())),
						STATE_SPECS, MUTUALLY_EXCLUSIVE_ACTIONS, REACTION_RULES, STATE_RULES, ImmutableSet.of(),
						GOALS));

		final PolicyGenerator.Result result = policyGenerator.generatePolicy();
		assertThat(result.isSuccess()).isTrue();
	}
}
