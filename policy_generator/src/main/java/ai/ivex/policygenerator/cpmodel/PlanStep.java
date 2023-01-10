package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/** @author Hoang Tung Dinh */
public final class PlanStep {
	private final Model model;
	private final ImmutableMap<String, Action> actionNameMap;
	private final ImmutableMap<String, ActionSpec> actionSpecMap;

	private final IntVar costVar;
	private final ImmutableMap<String, Constraint> changeableStateVars;
	private final List<ImmutableMap<String, Constraint>> alternativeChangeableStateVars;

	private final List<String> sortedActions;

	private PlanStep(Model model, ImmutableSet<ActionSpec> actionSpecs) {
		this.model = model;
		
		final ImmutableMap.Builder<String, Action> actionNameMapBuilder = ImmutableMap.builder();
		final ImmutableMap.Builder<String, ActionSpec> actionSpecMapBuilder = ImmutableMap.builder();
		this.sortedActions = new ArrayList<String>();

		actionSpecs.forEach(actionSpec -> {
			final Action action = Action.create(model, actionSpec.cost(), actionSpec.changeableStateMap(),
					actionSpec.changeableStateMapAlternatives());
			actionNameMapBuilder.put(actionSpec.name(), action);
			actionSpecMapBuilder.put(actionSpec.name(), actionSpec);
			sortedActions.add(actionSpec.name());
		});

		this.actionNameMap = actionNameMapBuilder.build();
		this.actionSpecMap = actionSpecMapBuilder.build();
		Collections.sort(this.sortedActions);

		this.costVar = model.intVar(Config.MIN_COST, Config.MAX_COST);
		final IntVar[] actionCosts = this.actionNameMap.values().stream().map(action -> action.getCostVar())
				.toArray(IntVar[]::new);
		model.sum(actionCosts, "=", costVar).post();

		final ImmutableMap.Builder<String, Constraint> changeableStateVarsBuilder = ImmutableMap.builder();

		checkArgument(actionSpecs.size() > 0, "There must be at least one action");
		final ObjectSet<String> states = actionSpecs.stream().findAny().get().changeableStateMap().keySet();
		states.forEach(state -> {
			final Constraint[] changeables = actionNameMap.values().stream()
					.map(action -> action.getChangeableStateVars().get(state)).toArray(Constraint[]::new);

			changeableStateVarsBuilder.put(state, model.or(changeables));
		});
		this.changeableStateVars = changeableStateVarsBuilder.build();

		this.alternativeChangeableStateVars = new ArrayList<ImmutableMap<String, Constraint>>();

		for (String action : sortedActions) { // Add each effect (nominal + alts) of each action to the list (Actions
											  // sorted alphabetically, alt effects have fixed order list)
			alternativeChangeableStateVars.add(actionNameMap.get(action).getChangeableStateVars());
			alternativeChangeableStateVars.addAll(actionNameMap.get(action).getAlternativeChangeableStateVars());
		}
	}

	static PlanStep create(Model model, ImmutableSet<ActionSpec> actionSpecs) {
		return new PlanStep(model, actionSpecs);
	}

	public Constraint getExecutingExactlyConstraint(ImmutableSet<String> actionNames) {
		Constraint constraint = model.trueConstraint();

		for (final String action : actionNameMap.keySet()) {
			if (actionNames.contains(action)) {
				constraint = model.and(constraint, getExecutingConstraint(action));
			} else {
				constraint = model.and(constraint, getNotExecutingConstraint(action));
			}
		}

		return constraint;
	}

	public Constraint getExecutingConstraint(String actionName) {
		return actionNameMap.get(actionName).getExecutingConstraint();
	}

	public Constraint getNotExecutingConstraint(String actionName) {
		return actionNameMap.get(actionName).getNotExecutingConstraint();
	}

	Constraint getMutuallyExclusiveConstraint(MutuallyExclusiveActions mutuallyExclusiveActions) {
		if (mutuallyExclusiveActions.actions().isEmpty()) {
			return model.trueConstraint();
		} else {
			return model.sum(
					mutuallyExclusiveActions.actions().stream()
							.map(action -> actionNameMap.get(action).getExecutingVar()).toArray(BoolVar[]::new),
					"<=", 1);
		}
	}

	IntVar getCostVar() {
		return costVar;
	}

	ImmutableMap<String, Constraint> getChangeableStateVars() {
		return changeableStateVars;
	}

	// INCLUDES NOMINALS (alternative means all alternatives in this case)
	List<ImmutableMap<String, Constraint>> getAlternativeChangeableStateVars() {
		return alternativeChangeableStateVars;
	}

	List<String> getSortedActions() {
		return sortedActions;
	}

	public ImmutableMap<String, ActionSpec> getActionSpecMap() {
		return actionSpecMap;
	}

	ImmutableSet<String> getExecutingActions() {
		return actionNameMap.entrySet().stream()
				.filter(entry -> entry.getValue().getExecutingConstraint().isSatisfied() == ESat.TRUE)
				.map(Map.Entry::getKey).collect(ImmutableSet.toImmutableSet());
	}
}
