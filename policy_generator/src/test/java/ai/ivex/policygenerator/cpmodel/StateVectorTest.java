package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
class StateVectorTest {

  private Model model;
  private final ImmutableSet<StateSpec> stateSpecs =
      ImmutableSet.of(
          StateSpec.create("s1", ImmutableSet.of("s1_1", "s1_2", "s1_3")),
          StateSpec.create("s2", ImmutableSet.of("s2_1", "s2_2", "s2_3")));

  @BeforeEach
  void setUp() {
    model = new Model();
  }

  @Test
  void testValue() {
    final StateVector stateVector = StateVector.create(model, stateSpecs);
    stateVector.getHasValueConstraint("s1", "s1_2").post();
    stateVector.getNotHasValueConstraint("s2", "s2_2").post();
    model.getSolver().solve();
    assertThat(stateVector.getValueName("s1")).matches("s1_2");
    assertThat(stateVector.getValueName("s2")).doesNotMatch("s2_2");
  }

  @Test
  void testGetValue() {
    final StateVector stateVector = StateVector.create(model, stateSpecs);
    stateVector.getHasValueConstraint("s1", "s1_2").post();
    stateVector.getHasValueConstraint("s2", "s2_2").post();
    model.getSolver().solve();
    assertThat(stateVector.getValue().getStateValueMap())
        .containsAllEntriesOf(ImmutableMap.of("s1", "s1_2", "s2", "s2_2"));
  }

  @Test
  void testHasSameValue() {
    final StateVector stateVector1 = StateVector.create(model, stateSpecs);
    final StateVector stateVector2 = StateVector.create(model, stateSpecs);
    stateVector1.getHasValueConstraint("s1", "s1_2").post();
    stateVector2.getHasSameValueConstraint("s1", stateVector1).post();
    model.getSolver().solve();
    assertThat(stateVector2.getValueName("s1")).matches("s1_2");
  }
}
