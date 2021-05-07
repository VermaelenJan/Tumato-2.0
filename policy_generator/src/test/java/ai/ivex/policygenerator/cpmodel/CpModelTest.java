package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
class CpModelTest {

  private static final int PLAN_LENGTH = 3;
  private static final ImmutableSet<ActionSpec> ACTION_SPECS =
      ImmutableSet.of(
          ActionSpec.create(
              "take_off",
              1,
              new Object2BooleanOpenHashMap<>(
                  new String[] {"S_flying", "S_destination"}, new boolean[] {true, false}),
              (model, stateVector) ->
                  stateVector.getHasValueConstraint("S_flying", "on_the_ground"),
              (model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying")),
          ActionSpec.create(
              "land",
              1,
              new Object2BooleanOpenHashMap<>(
                  new String[] {"S_flying", "S_destination"}, new boolean[] {true, false}),
              (model, stateVector) -> stateVector.getHasValueConstraint("S_flying", "flying"),
              (model, stateVector) ->
                  stateVector.getHasValueConstraint("S_flying", "on_the_ground")),
          ActionSpec.create(
              "go_to_waypoint",
              1,
              new Object2BooleanOpenHashMap<>(
                  new String[] {"S_flying", "S_destination"}, new boolean[] {false, true}),
              (model, stateVector) ->
                  model.and(
                      stateVector.getHasValueConstraint("S_destination", "not_reached"),
                      stateVector.getHasValueConstraint("S_flying", "flying")),
              (model, stateVector) ->
                  stateVector.getHasValueConstraint("S_destination", "reached")),
          ActionSpec.create(
              "turn_on_led_light",
              1,
              new Object2BooleanOpenHashMap<>(
                  new String[] {"S_flying", "S_destination"}, new boolean[] {false, false}),
              (model, stateVector) -> model.trueConstraint(),
              (model, stateVector) -> model.trueConstraint()));
  private static final ImmutableSet<StateSpec> STATE_SPECS =
      ImmutableSet.of(
          StateSpec.create("S_flying", ImmutableSet.of("on_the_ground", "flying")),
          StateSpec.create("S_destination", ImmutableSet.of("not_reached", "reached")));
  private static final StateVectorValue INITIAL_STATES =
      StateVectorValue.create(
          ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "not_reached"));
  private static final ImmutableSet<MutuallyExclusiveActions> MUTUALLY_EXCLUSIVE_ACTIONS =
      ImmutableSet.of(
          MutuallyExclusiveActions.create(ImmutableSet.of("take_off", "go_to_waypoint", "land")));
  private static final ImmutableSet<ReactionRule> REACTION_RULES =
      ImmutableSet.of(
          (model, stateVector, planStep) ->
              model.ifThen(
                  planStep.getExecutingConstraint("go_to_waypoint"),
                  planStep.getExecutingConstraint("turn_on_led_light")));
  private static final Goals GOALS =
      (model, initialStateValue, initialStateVector, firstPlanStep, goalStateVector) -> {
        goalStateVector.getHasValueConstraint("S_flying", "on_the_ground").post();
        goalStateVector.getHasValueConstraint("S_destination", "reached").post();
      };

  @Test
  void testCpModel() {
    final CpModel cpModel =
        CpModel.create(
            PLAN_LENGTH,
            ACTION_SPECS,
            STATE_SPECS,
            INITIAL_STATES,
            MUTUALLY_EXCLUSIVE_ACTIONS,
            REACTION_RULES,
            ImmutableSet.of(),
            GOALS);

    final Optional<ResultingPlan> plan = cpModel.solve();

    assertThat(plan).isNotEmpty();
    assertThat(plan.get().mappings())
        .containsExactlyInAnyOrder(
            Mapping.create(
                ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "not_reached"),
                ImmutableSet.of("take_off")),
            Mapping.create(
                ImmutableMap.of("S_flying", "flying", "S_destination", "not_reached"),
                ImmutableSet.of("go_to_waypoint", "turn_on_led_light")),
            Mapping.create(
                ImmutableMap.of("S_flying", "flying", "S_destination", "reached"),
                ImmutableSet.of("land")));
  }

  @Test
  void testAssumptions() {
    final CpModel cpModel =
        CpModel.create(
            PLAN_LENGTH,
            ACTION_SPECS,
            STATE_SPECS,
            INITIAL_STATES,
            MUTUALLY_EXCLUSIVE_ACTIONS,
            REACTION_RULES,
            ImmutableSet.of(
                (model, stateVector) ->
                    stateVector.getHasValueConstraint("S_flying", "on_the_ground").post()),
            GOALS);

    final Optional<ResultingPlan> plan = cpModel.solve();

    assertThat(plan).isEmpty();
  }

  @Test
  void testConflictMapping() {
    final CpModel cpModel =
        CpModel.create(
            PLAN_LENGTH,
            ACTION_SPECS,
            STATE_SPECS,
            INITIAL_STATES,
            MUTUALLY_EXCLUSIVE_ACTIONS,
            REACTION_RULES,
            ImmutableSet.of(
                (model, stateVector) ->
                    stateVector.getHasValueConstraint("S_flying", "on_the_ground").post()),
            GOALS,
            ImmutableSet.of(
                Mapping.create(
                    StateVectorValue.create(
                        ImmutableMap.of("S_flying", "flying", "S_destination", "not_reached")),
                        ImmutableSet.of("go_to_waypoint", "land"))));

    final Optional<ResultingPlan> plan = cpModel.solve();

    assertThat(plan).isEmpty();
  }
}
