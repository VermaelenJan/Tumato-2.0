package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class Mapping {

  static Mapping create(StateVectorValue states, ImmutableSet<String> actions) {
    return new AutoValue_Mapping(states, actions);
  }

  @VisibleForTesting
  public static Mapping create(ImmutableMap<String, String> states, ImmutableSet<String> actions) {
    return new AutoValue_Mapping(StateVectorValue.create(states), actions);
  }

  public abstract StateVectorValue states();

  public abstract ImmutableSet<String> actions();
}
