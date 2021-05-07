package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/** @author Hoang Tung Dinh */
final class CpModel {
  private final ImmutableSet<ActionSpec> actionSpecs;
  private final StateVectorValue initialStates;
  private final ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions;
  private final ImmutableSet<ReactionRule> reactionRules;
  private final ImmutableSet<Assumption> assumptions;
  private final ImmutableSet<Mapping> existingMappings;
  private final Goals goals;

  private final Model model = new Model();
  private final ImmutableList<PlanStep> planSteps;
  private final ImmutableList<StateVector> stateVectors;

  private CpModel(
      int planLength,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<StateSpec> stateSpecs,
      StateVectorValue initialStates,
      ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions,
      ImmutableSet<ReactionRule> reactionRules,
      ImmutableSet<Assumption> assumptions,
      Goals goals,
      ImmutableSet<Mapping> existingMappings) {
    checkArgument(planLength >= 1, "Plan length must be at least 1.");
    this.actionSpecs = actionSpecs;
    this.initialStates = initialStates;
    this.mutuallyExclusiveActions = mutuallyExclusiveActions;
    this.reactionRules = reactionRules;
    this.assumptions = assumptions;
    this.existingMappings = existingMappings;
    this.goals = goals;
    final ImmutableList.Builder<PlanStep> planStepBuilder = ImmutableList.builder();
    for (int i = 0; i < planLength; i++) {
      planStepBuilder.add(PlanStep.create(model, actionSpecs));
    }
    this.planSteps = planStepBuilder.build();

    final ImmutableList.Builder<StateVector> stateVectorBuilder = ImmutableList.builder();
    for (int i = 0; i < planLength + 1; i++) {
      stateVectorBuilder.add(StateVector.create(model, stateSpecs));
    }
    this.stateVectors = stateVectorBuilder.build();
  }

  static CpModel create(
      int planLength,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<StateSpec> stateSpecs,
      StateVectorValue initialStates,
      ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions,
      ImmutableSet<ReactionRule> reactionRules,
      ImmutableSet<Assumption> assumptions,
      Goals goals) {
    return new CpModel(
        planLength,
        actionSpecs,
        stateSpecs,
        initialStates,
        mutuallyExclusiveActions,
        reactionRules,
        assumptions,
        goals,
        ImmutableSet.of());
  }

  static CpModel create(
      int planLength,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<StateSpec> stateSpecs,
      StateVectorValue initialStates,
      ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions,
      ImmutableSet<ReactionRule> reactionRules,
      ImmutableSet<Assumption> assumptions,
      Goals goals,
      ImmutableSet<Mapping> existingMappings) {
    return new CpModel(
        planLength,
        actionSpecs,
        stateSpecs,
        initialStates,
        mutuallyExclusiveActions,
        reactionRules,
        assumptions,
        goals,
        existingMappings);
  }

  Optional<ResultingPlan> solve() {
    addInitialStateConstraint();
    addMutuallyExclusiveConstraint();
    addChangeableConstraint();
    addPreconditionAndEffectConstraint();
    addReactionRules();
    addAssumptions();
    addExistingMappings();
    addGoals();

    final IntVar cost = getTotalCost();

    model.setObjective(Model.MINIMIZE, cost);

    final boolean hasSolution = model.getSolver().solve();

    if (hasSolution) {
      return getSolution();
    } else {
      return Optional.empty();
    }
  }

  private void addInitialStateConstraint() {
    final StateVector initialStateVector = stateVectors.get(0);
    initialStates
        .getStateValueMap()
        .forEach((state, value) -> initialStateVector.getHasValueConstraint(state, value).post());
  }

  private void addMutuallyExclusiveConstraint() {
    mutuallyExclusiveActions.forEach(
        actions ->
            planSteps.forEach(planStep -> planStep.getMutuallyExclusiveConstraint(actions).post()));
  }

  private void addChangeableConstraint() {
    for (int i = 0; i < planSteps.size(); i++) {
      final ImmutableMap<String, Constraint> changeables =
          planSteps.get(i).getChangeableStateVars();
      final StateVector previousStateVector = stateVectors.get(i);
      final StateVector nextStateVector = stateVectors.get(i + 1);

      changeables.forEach(
          (state, changeable) ->
              model.ifThen(
                  model.not(changeable),
                  nextStateVector.getHasSameValueConstraint(state, previousStateVector)));
    }
  }

  private void addPreconditionAndEffectConstraint() {
    for (int i = 0; i < planSteps.size(); i++) {
      final PlanStep planStep = planSteps.get(i);
      final StateVector previousStateVector = stateVectors.get(i);
      final StateVector nextStateVector = stateVectors.get(i + 1);

      actionSpecs.forEach(
          actionSpec ->
              model.ifThen(
                  planStep.getExecutingConstraint(actionSpec.name()),
                  model.and(
                      actionSpec.precondition().getConstraint(model, previousStateVector),
                      actionSpec.effect().getConstraint(model, nextStateVector))));
    }
  }

  private void addReactionRules() {
    for (int i = 0; i < planSteps.size(); i++) {
      final PlanStep planStep = planSteps.get(i);
      final StateVector stateVector = stateVectors.get(i);
      reactionRules.forEach(
          reactionRule -> reactionRule.applyConstraint(model, stateVector, planStep));
    }
  }

  private void addAssumptions() {
    stateVectors.forEach(
        stateVector ->
            assumptions.forEach(assumption -> assumption.applyConstraint(model, stateVector)));
  }

  private void addExistingMappings() {
    existingMappings.forEach(
        mapping -> {
          for (int i = 0; i < planSteps.size(); i++) {
            final StateVector stateVector = stateVectors.get(i);
            final PlanStep planStep = planSteps.get(i);
            model.ifThen(
                stateVector.getHasValueConstraint(mapping.states()),
                planStep.getExecutingExactlyConstraint(mapping.actions()));
          }
        });
  }

  private void addGoals() {
    goals.applyGoals(
        model,
        initialStates,
        stateVectors.get(0),
        planSteps.get(0),
        stateVectors.get(stateVectors.size() - 1));
  }

  private IntVar getTotalCost() {
    final IntVar cost = model.intVar(Config.MIN_COST, Config.MAX_COST);
    model
        .sum(
            planSteps.stream().map(planStep -> planStep.getCostVar()).toArray(IntVar[]::new),
            "=",
            cost)
        .post();

    return cost;
  }

  private Optional<ResultingPlan> getSolution() {
    final ImmutableList.Builder<Mapping> mappingBuilder = ImmutableList.builder();
    for (int i = 0; i < planSteps.size(); i++) {
      mappingBuilder.add(
          Mapping.create(stateVectors.get(i).getValue(), planSteps.get(i).getExecutingActions()));
    }
    return Optional.of(ResultingPlan.create(mappingBuilder.build()));
  }
}
