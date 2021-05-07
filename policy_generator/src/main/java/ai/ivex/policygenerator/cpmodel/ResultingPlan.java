package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/** @author Hoang Tung Dinh */
@AutoValue
abstract class ResultingPlan {

  static ResultingPlan create(ImmutableList<Mapping> mappings) {
    return new AutoValue_ResultingPlan(mappings);
  }

  abstract ImmutableList<Mapping> mappings();
}
