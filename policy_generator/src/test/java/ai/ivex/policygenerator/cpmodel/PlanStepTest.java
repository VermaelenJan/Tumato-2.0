package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.util.ESat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

/** @author Hoang Tung Dinh */
class PlanStepTest {
	@Test
	void testPlanStep() {
		final Model model = new Model();

		final Object2BooleanMap<String> a1Changeable = new Object2BooleanOpenHashMap<>();
		a1Changeable.put("s1", true);
		a1Changeable.put("s2", false);
		a1Changeable.put("s3", false);
		List<Object2BooleanMap<String>> a1ChangeablesAlternative = new ArrayList<Object2BooleanMap<String>>();

		final Object2BooleanMap<String> a2Changeable = new Object2BooleanOpenHashMap<>();
		a2Changeable.put("s1", false);
		a2Changeable.put("s2", true);
		a2Changeable.put("s3", false);
		List<Object2BooleanMap<String>> a2ChangeablesAlternative = new ArrayList<Object2BooleanMap<String>>();

		final ImmutableSet<ActionSpec> actionSpecs = ImmutableSet.of(
				ActionSpec.create("a1", 5, a1Changeable, a1ChangeablesAlternative, mock(Precondition.class),
						mock(Effect.class), new ArrayList<Effect>()),
				ActionSpec.create("a2", 2, a2Changeable, a2ChangeablesAlternative, mock(Precondition.class),
						mock(Effect.class), new ArrayList<Effect>()));

		final PlanStep planStep = PlanStep.create(model, actionSpecs);
		planStep.getExecutingConstraint("a1").post();
		planStep.getNotExecutingConstraint("a2").post();

		final boolean hasSolution = model.getSolver().solve();

		assertThat(hasSolution).isTrue();
		assertThat(planStep.getCostVar().getValue()).isEqualTo(5);
		assertThat(planStep.getExecutingConstraint("a1").isSatisfied()).isEqualByComparingTo(ESat.TRUE);
		assertThat(planStep.getExecutingConstraint("a2").isSatisfied()).isEqualByComparingTo(ESat.FALSE);
		assertThat(planStep.getChangeableStateVars().get("s1").isSatisfied()).isEqualByComparingTo(ESat.TRUE);
		assertThat(planStep.getChangeableStateVars().get("s2").isSatisfied()).isEqualByComparingTo(ESat.FALSE);
		assertThat(planStep.getChangeableStateVars().get("s3").isSatisfied()).isEqualByComparingTo(ESat.FALSE);
		assertThat(planStep.getExecutingActions()).containsExactlyInAnyOrder("a1");
	}

	@Test
	void testMutuallyExclusiveConstraint() {
		final Object2BooleanMap<String> changable = new Object2BooleanOpenHashMap<>();
		changable.put("s1", true);
		changable.put("s2", true);
		changable.put("s3", true);
		List<Object2BooleanMap<String>> changeablesAlternative = new ArrayList<Object2BooleanMap<String>>();

		final ImmutableSet<ActionSpec> actionSpecs = ImmutableSet.of(
				ActionSpec.create("a1", 5, changable, changeablesAlternative, mock(Precondition.class),
						mock(Effect.class), new ArrayList<Effect>()),
				ActionSpec.create("a2", 2, changable, changeablesAlternative, mock(Precondition.class),
						mock(Effect.class), new ArrayList<Effect>()),
				ActionSpec.create("a3", 3, changable, changeablesAlternative, mock(Precondition.class),
						mock(Effect.class), new ArrayList<Effect>()));

		final Model model = new Model();

		final PlanStep planStep = PlanStep.create(model, actionSpecs);

		planStep.getMutuallyExclusiveConstraint(MutuallyExclusiveActions.create(ImmutableSet.of("a1", "a2", "a3")))
				.post();

		planStep.getExecutingConstraint("a1").post();

		model.getSolver().solve();

		assertThat(planStep.getExecutingActions()).containsExactlyInAnyOrder("a1");
	}
}
