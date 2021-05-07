package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class ActionSpec {

  public static ActionSpec create(
      String name,
      int cost,
      Object2BooleanMap<String> changeableStateMap,
      Precondition precondition,
      Effect effect) {
    return new AutoValue_ActionSpec(name, cost, changeableStateMap, precondition, effect);
  }

  abstract String name();

  abstract int cost();

  abstract Object2BooleanMap<String> changeableStateMap();

  abstract Precondition precondition();

  abstract Effect effect();
}
