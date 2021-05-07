package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.util.ESat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** @author Hoang Tung Dinh */
class PlanStepTest {
  @Test
  void testPlanStep() {
    final Model model = new Model();

    final Object2BooleanMap<String> a1Changeable = new Object2BooleanOpenHashMap<>();
    a1Changeable.put("s1", true);
    a1Changeable.put("s2", false);
    a1Changeable.put("s3", false);

    final Object2BooleanMap<String> a2Changeable = new Object2BooleanOpenHashMap<>();
    a2Changeable.put("s1", false);
    a2Changeable.put("s2", true);
    a2Changeable.put("s3", false);

    final ImmutableSet<ActionSpec> actionSpecs =
        ImmutableSet.of(
            ActionSpec.create("a1", 5, a1Changeable, mock(Precondition.class), mock(Effect.class)),
            ActionSpec.create("a2", 2, a2Changeable, mock(Precondition.class), mock(Effect.class)));

    final PlanStep planStep = PlanStep.create(model, actionSpecs);
    planStep.getExecutingConstraint("a1").post();
    planStep.getNotExecutingConstraint("a2").post();

    final boolean hasSolution = model.getSolver().solve();

    assertThat(hasSolution).isTrue();
    assertThat(planStep.getCostVar().getValue()).isEqualTo(5);
    assertThat(planStep.getExecutingConstraint("a1").isSatisfied()).isEqualByComparingTo(ESat.TRUE);
    assertThat(planStep.getExecutingConstraint("a2").isSatisfied())
        .isEqualByComparingTo(ESat.FALSE);
    assertThat(planStep.getChangeableStateVars().get("s1").isSatisfied())
        .isEqualByComparingTo(ESat.TRUE);
    assertThat(planStep.getChangeableStateVars().get("s2").isSatisfied())
        .isEqualByComparingTo(ESat.FALSE);
    assertThat(planStep.getChangeableStateVars().get("s3").isSatisfied())
        .isEqualByComparingTo(ESat.FALSE);
    assertThat(planStep.getExecutingActions()).containsExactlyInAnyOrder("a1");
  }

  @Test
  void testMutuallyExclusiveConstraint() {
    final Object2BooleanMap<String> changable = new Object2BooleanOpenHashMap<>();
    changable.put("s1", true);
    changable.put("s2", true);
    changable.put("s3", true);

    final ImmutableSet<ActionSpec> actionSpecs =
        ImmutableSet.of(
            ActionSpec.create("a1", 5, changable, mock(Precondition.class), mock(Effect.class)),
            ActionSpec.create("a2", 2, changable, mock(Precondition.class), mock(Effect.class)),
            ActionSpec.create("a3", 3, changable, mock(Precondition.class), mock(Effect.class)));

    final Model model = new Model();

    final PlanStep planStep = PlanStep.create(model, actionSpecs);

    planStep
        .getMutuallyExclusiveConstraint(
            MutuallyExclusiveActions.create(ImmutableSet.of("a1", "a2", "a3")))
        .post();

    planStep.getExecutingConstraint("a1").post();

    model.getSolver().solve();

    assertThat(planStep.getExecutingActions()).containsExactlyInAnyOrder("a1");
  }
}
