package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class MutuallyExclusiveActions {

  public static MutuallyExclusiveActions create(ImmutableSet<String> actions) {
    return new AutoValue_MutuallyExclusiveActions(actions);
  }

  abstract ImmutableSet<String> actions();
}
