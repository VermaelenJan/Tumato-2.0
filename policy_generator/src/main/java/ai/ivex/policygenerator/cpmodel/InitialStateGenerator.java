package ai.ivex.policygenerator.cpmodel;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** @author Hoang Tung Dinh */
final class InitialStateGenerator {
  private final ImmutableSet<StateSpec> stateSpecs;

  private InitialStateGenerator(ImmutableSet<StateSpec> stateSpecs) {
    this.stateSpecs = stateSpecs;
  }

  static InitialStateGenerator create(ImmutableSet<StateSpec> stateSpecs) {
    return new InitialStateGenerator(stateSpecs);
  }

  void generateInitialStates(Consumer<StateVectorValue> consumer) {
    long numSolutions = 1L;
    for (final StateSpec stateSpec : stateSpecs) {
      numSolutions *= stateSpec.values().size();
    }

    final ImmutableMap<String, ImmutableList<String>> nameValuesMap =
        stateSpecs
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    StateSpec::name, stateSpec -> ImmutableList.copyOf(stateSpec.values())));
    for (long i = 0L; i < numSolutions; i++) {
      final ImmutableMap.Builder<String, String> stateValueBuilder = ImmutableMap.builder();
      long j = 1L;
      for (final Map.Entry<String, ImmutableList<String>> entry : nameValuesMap.entrySet()) {
        stateValueBuilder.put(
            entry.getKey(), entry.getValue().get((int) (i / j) % entry.getValue().size()));
        j *= entry.getValue().size();
      }
      consumer.accept(StateVectorValue.create(stateValueBuilder.build()));
    }
  }

  Set<StateVectorValue> generateInitialStates() {
    final List<Set<StateValue>> input = new ArrayList<>();
    stateSpecs.forEach(
        stateSpec ->
            input.add(
                stateSpec
                    .values()
                    .stream()
                    .map(value -> StateValue.create(stateSpec.name(), value))
                    .collect(Collectors.toSet())));

    final Set<List<StateValue>> catersianProduct = Sets.cartesianProduct(input);

    return catersianProduct
        .stream()
        .map(
            stateValues ->
                StateVectorValue.create(
                    stateValues
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(StateValue::state, StateValue::value))))
        .collect(Collectors.toSet());
  }

  @AutoValue
  abstract static class StateValue {

    static StateValue create(String state, String value) {
      return new AutoValue_InitialStateGenerator_StateValue(state, value);
    }

    abstract String state();

    abstract String value();
  }
}
