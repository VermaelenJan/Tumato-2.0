package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** @author Hoang Tung Dinh */
public final class PolicyGenerator {

  private static final Logger logger = LoggerFactory.getLogger(PolicyGenerator.class);

  private final int maxPlanLength;
  private final ImmutableSet<ActionSpec> actionSpecs;
  private final ImmutableSet<StateSpec> stateSpecs;
  private final ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions;
  private final ImmutableSet<ReactionRule> reactionRules;
  private final ImmutableSet<StateRule> stateRules;
  private final ImmutableSet<Assumption> assumptions;
  private final Goals goals;

  private final Map<StateVectorValue, ImmutableSet<String>> policyMap = new ConcurrentHashMap<>();

  private PolicyGenerator(
      int maxPlanLength,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<StateSpec> stateSpecs,
      ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions,
      ImmutableSet<ReactionRule> reactionRules,
      ImmutableSet<StateRule> stateRules,
      ImmutableSet<Assumption> assumptions,
      Goals goals) {
    this.maxPlanLength = maxPlanLength;
    this.actionSpecs = actionSpecs;
    this.stateSpecs = stateSpecs;
    this.mutuallyExclusiveActions = mutuallyExclusiveActions;
    this.reactionRules = reactionRules;
    this.stateRules = stateRules;
    this.assumptions = assumptions;
    this.goals = goals;
  }

  public static PolicyGenerator create(Specification specification) {
    return new PolicyGenerator(
        specification.maxPlanLength(),
        specification.actionSpecs(),
        specification.stateSpecs(),
        specification.mutuallyExclusiveActions(),
        specification.reactionRules(),
        specification.stateRules(),
        specification.assumptions(),
        specification.goals());
  }

  public Result generatePolicy() {
    final InitialStateGenerator initialStateGenerator = InitialStateGenerator.create(stateSpecs);
    final Set<StateVectorValue> allStates = initialStateGenerator.generateInitialStates();

    logger.info("Total number of states: " + allStates.size());

    final Set<StateVectorValue> validStates =
        allStates
            .parallelStream()
            .filter(
                stateVectorValue -> {
                  final boolean isValidState = isValidState(stateVectorValue);
                  if (!isValidState) {
                    policyMap.put(stateVectorValue, ImmutableSet.of());
                  }
                  return isValidState;
                })
            .collect(Collectors.toSet());

    logger.info("Total number of valid states: " + validStates.size());

    final ExecutorService executorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    final ExecutorCompletionService<PlanningResult> executorCompletionService =
        new ExecutorCompletionService<>(executorService);

    validStates.forEach(initialState -> executorCompletionService.submit(() -> plan(initialState)));

    PlanningStatus planningStatus = PlanningStatus.SUCCEEDED;
    StateVectorValue failedState = null;
    for (int i = 0; i < validStates.size(); i++) {
      try {
        final PlanningResult planningResult = executorCompletionService.take().get();
        if (planningResult.getPlanningStatus() == PlanningStatus.FAILED) {
          planningStatus = PlanningStatus.FAILED;
          failedState = planningResult.getInitialState();
          logger.info("No solution for the following state: " + planningResult.getInitialState());
          break;
        }
      } catch (InterruptedException | ExecutionException e) {
        planningStatus = PlanningStatus.FAILED;
        logger.error("Weird! Exception occurs");
        break;
      }

      logger.info("Number of solved problems: " + i);
    }

    executorService.shutdownNow();
    if (planningStatus == PlanningStatus.SUCCEEDED) {
      return Result.createSuccessResultWithPolicy(
          Policy.create(
              stateSpecs,
              actionSpecs,
              policyMap
                  .entrySet()
                  .stream()
                  .map(mapping -> Mapping.create(mapping.getKey(), mapping.getValue()))
                  .collect(ImmutableSet.toImmutableSet())));
    } else {
      return Result.createFailureResultWithFailedState(failedState);
    }
  }

  private boolean isValidState(StateVectorValue stateVectorValue) {
    final Model model = new Model();
    final StateVector stateVector = StateVector.create(model, stateSpecs);
    assumptions.forEach(assumption -> assumption.applyConstraint(model, stateVector));
    stateVectorValue
        .getStateValueMap()
        .forEach((state, value) -> stateVector.getHasValueConstraint(state, value).post());
    return model.getSolver().solve();
  }

  private PlanningResult plan(StateVectorValue initialStates) {
    for (int planLength = 1; planLength <= maxPlanLength; planLength++) {
      final PlanningStatus planningStatus = plan(initialStates, planLength);
      if (planningStatus == PlanningStatus.SUCCEEDED) {
        return PlanningResult.create(PlanningStatus.SUCCEEDED, initialStates);
      }
    }

    return PlanningResult.create(PlanningStatus.FAILED, initialStates);
  }

  private PlanningStatus plan(StateVectorValue initialStates, int planLength) {
    // TODO(tung): this set can be reused in different plan length
    final Set<Mapping> conflictMappings = new HashSet<>();

    while (true) {
      final CpModel cpModel =
          CpModel.create(
              planLength,
              actionSpecs,
              stateSpecs,
              initialStates,
              mutuallyExclusiveActions,
              reactionRules,
              stateRules,
              assumptions,
              goals,
              ImmutableSet.copyOf(conflictMappings));
      final Optional<ResultingPlan> plan = cpModel.solve();

      if (plan.isPresent()) {
        synchronized (policyMap) {
          final Set<Mapping> newConflictMappings = getConflictMappings(plan.get());
          if (newConflictMappings.isEmpty()) {
            plan.get()
                .mappings()
                .forEach(mapping -> policyMap.put(mapping.states(), mapping.actions()));
            return PlanningStatus.SUCCEEDED;
          } else {
            conflictMappings.addAll(newConflictMappings);
          }
        }
      } else {
        return PlanningStatus.FAILED;
      }
    }
  }

  private Set<Mapping> getConflictMappings(ResultingPlan resultingPlan) {
    return resultingPlan
        .mappings()
        .stream()
        .filter(
            mapping ->
                policyMap.containsKey(mapping.states())
                    && !policyMap.get(mapping.states()).equals(mapping.actions()))
        .map(mapping -> Mapping.create(mapping.states(), policyMap.get(mapping.states())))
        .collect(Collectors.toSet());
  }

  private static final class PlanningResult {
    private final PlanningStatus planningStatus;
    private final StateVectorValue initialState;

    private PlanningResult(PlanningStatus planningStatus, StateVectorValue initialState) {
      this.planningStatus = planningStatus;
      this.initialState = initialState;
    }

    public static PlanningResult create(
        PlanningStatus planningStatus, StateVectorValue initialState) {
      return new PlanningResult(planningStatus, initialState);
    }

    public PlanningStatus getPlanningStatus() {
      return planningStatus;
    }

    public StateVectorValue getInitialState() {
      return initialState;
    }
  }

  private enum PlanningStatus {
    SUCCEEDED,
    FAILED
  }

  public static final class Result {
    private final boolean success;
    @Nullable private final StateVectorValue failedState;
    @Nullable private final Policy policy;

    private Result(
        boolean success, @Nullable StateVectorValue failedState, @Nullable Policy policy) {
      this.success = success;
      this.failedState = failedState;
      this.policy = policy;
    }

    private static Result createSuccessResultWithPolicy(Policy policy) {
      return new Result(true, null, policy);
    }

    private static Result createFailureResultWithFailedState(StateVectorValue failedState) {
      return new Result(false, failedState, null);
    }

    public boolean isSuccess() {
      return success;
    }

    public Optional<StateVectorValue> getFailedState() {
      return Optional.ofNullable(failedState);
    }

    public Optional<Policy> getPolicy() {
      return Optional.ofNullable(policy);
    }
  }
}
