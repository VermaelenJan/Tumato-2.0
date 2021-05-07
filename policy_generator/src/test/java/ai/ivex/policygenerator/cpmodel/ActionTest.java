package ai.ivex.policygenerator.cpmodel;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.util.ESat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
class ActionTest {
  @Test
  void testActionExecuting() {
    final Model model = new Model();
    final int cost = 5;
    Object2BooleanMap<String> changeableState = new Object2BooleanOpenHashMap<>();
    changeableState.put("first_state", true);
    changeableState.put("second_state", false);

    final Action action = Action.create(model, cost, changeableState);

    action.getExecutingConstraint().post();

    final boolean hasSolution = model.getSolver().solve();

    assertThat(hasSolution).isTrue();
    assertThat(action.getCostVar().getValue()).isEqualTo(cost);
    assertThat(action.getChangeableStateVars().get("first_state").isSatisfied())
        .isEqualByComparingTo(ESat.TRUE);
    assertThat(action.getChangeableStateVars().get("second_state").isSatisfied())
        .isEqualByComparingTo(ESat.FALSE);
  }

  @Test
  void testActionNotExecuting() {
    final Model model = new Model();
    final int cost = 5;
    Object2BooleanMap<String> changeableState = new Object2BooleanOpenHashMap<>();
    changeableState.put("first_state", true);
    changeableState.put("second_state", false);

    final Action action = Action.create(model, cost, changeableState);

    action.getNotExecutingConstraint().post();

    final boolean hasSolution = model.getSolver().solve();

    assertThat(hasSolution).isTrue();
    assertThat(action.getCostVar().getValue()).isEqualTo(0);
    assertThat(action.getChangeableStateVars().get("first_state").isSatisfied())
        .isEqualByComparingTo(ESat.FALSE);
    assertThat(action.getChangeableStateVars().get("second_state").isSatisfied())
        .isEqualByComparingTo(ESat.FALSE);
  }
}
