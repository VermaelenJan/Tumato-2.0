package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
class StateTest {
  private Model model;
  private State state;

  @BeforeEach
  void setUp() {
    model = new Model();

    final ImmutableSet<String> valueNames = ImmutableSet.of("s1_a", "s1_b", "s1_c");
    state = State.create(model, "s1", NameValueBiMap.create(valueNames));
  }

  @Test
  void testHasValue() {
    state.getHasValueConstraint("s1_a").post();
    final boolean hasSolution = model.getSolver().solve();

    assertThat(hasSolution).isTrue();
    assertThat(state.getValueName()).matches("s1_a");
  }

  @Test
  void testNotHasValue() {
    state.getNotHasValueConstraint("s1_a").post();
    final boolean hasSolution = model.getSolver().solve();

    assertThat(hasSolution).isTrue();
    assertThat(state.getValueName()).doesNotMatch("s1_a");
  }

  @Test
  void testHasSameValue() {
    final State anotherState =
        State.create(model, "s1", NameValueBiMap.create(ImmutableSet.of("s1_a", "s1_b", "s1_c")));
    anotherState.getHasValueConstraint("s1_b").post();

    state.getHasSameValueConstraint(anotherState).post();
    final boolean hasSolution = model.getSolver().solve();

    assertThat(hasSolution).isTrue();
    assertThat(state.getValueName()).matches("s1_b");
  }
}
