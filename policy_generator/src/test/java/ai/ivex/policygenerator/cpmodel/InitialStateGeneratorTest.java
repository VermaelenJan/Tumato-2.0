package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
class InitialStateGeneratorTest {
  @Test
  void testCorrectGeneratedStates() {
    final ImmutableSet<StateSpec> stateSpecs =
        ImmutableSet.of(
            StateSpec.create("s1", ImmutableSet.of("1")),
            StateSpec.create("s2", ImmutableSet.of("1", "2")),
            StateSpec.create("s3", ImmutableSet.of("1", "2", "3")));

    final InitialStateGenerator initialStateGenerator = InitialStateGenerator.create(stateSpecs);
    final Set<StateVectorValue> generatedStates = new HashSet<>();
    initialStateGenerator.generateInitialStates(generatedStates::add);

    assertThat(generatedStates)
        .containsExactlyInAnyOrder(
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "1", "s3", "1")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "1", "s3", "2")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "1", "s3", "3")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "2", "s3", "1")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "2", "s3", "2")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "2", "s3", "3")));

    assertThat(initialStateGenerator.generateInitialStates())
        .containsExactlyInAnyOrder(
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "1", "s3", "1")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "1", "s3", "2")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "1", "s3", "3")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "2", "s3", "1")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "2", "s3", "2")),
            StateVectorValue.create(ImmutableMap.of("s1", "1", "s2", "2", "s3", "3")));
  }
}
