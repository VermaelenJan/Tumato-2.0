package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class StateSpec {

  public static StateSpec create(String name, ImmutableSet<String> values) {
    return new AutoValue_StateSpec(name, values);
  }

  abstract String name();

  abstract ImmutableSet<String> values();
}
