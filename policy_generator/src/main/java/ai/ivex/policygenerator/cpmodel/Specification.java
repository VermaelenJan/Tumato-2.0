package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class Specification {

  public static Specification create(
      int maxPlanLength,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<StateSpec> stateSpecs,
      ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions,
      ImmutableSet<ReactionRule> reactionRules,
      ImmutableSet<Assumption> assumptions,
      Goals goals) {
    return new AutoValue_Specification(
        maxPlanLength,
        actionSpecs,
        stateSpecs,
        mutuallyExclusiveActions,
        reactionRules,
        assumptions,
        goals);
  }

  @VisibleForTesting
  public abstract int maxPlanLength();

  @VisibleForTesting
  public abstract ImmutableSet<ActionSpec> actionSpecs();

  @VisibleForTesting
  public abstract ImmutableSet<StateSpec> stateSpecs();

  @VisibleForTesting
  public abstract ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions();

  @VisibleForTesting
  public abstract ImmutableSet<ReactionRule> reactionRules();

  @VisibleForTesting
  public abstract ImmutableSet<Assumption> assumptions();

  @VisibleForTesting
  public abstract Goals goals();
}
