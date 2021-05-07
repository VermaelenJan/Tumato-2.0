package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

/** @author Hoang Tung Dinh */
final class Action {
  private final Model model;

  private final BoolVar executingVar;
  private final IntVar costVar;
  private final ImmutableMap<String, Constraint> changeableStateVars;

  private Action(Model model, int cost, Object2BooleanMap<String> changeableStateMap) {
    this.model = model;

    this.executingVar = model.boolVar();

    this.costVar = model.intVar(new int[] {0, cost});
    model.ifThenElse(executingVar, model.arithm(costVar, "=", cost), model.arithm(costVar, "=", 0));

    final ImmutableMap.Builder<String, Constraint> changeableStateVarsBuilder =
        ImmutableMap.builder();

    changeableStateMap.forEach(
        (state, changeable) -> {
          final Constraint changeableConstraint;
          if (changeable) {
            changeableConstraint = model.arithm(executingVar, "=", 1);
          } else {
            changeableConstraint = model.falseConstraint();
          }

          changeableStateVarsBuilder.put(state, changeableConstraint);
        });

    changeableStateVars = changeableStateVarsBuilder.build();
  }

  static Action create(Model model, int cost, Object2BooleanMap<String> changeableStateMap) {
    return new Action(model, cost, changeableStateMap);
  }

  Constraint getExecutingConstraint() {
    return model.arithm(executingVar, "=", 1);
  }

  Constraint getNotExecutingConstraint() {
    return model.arithm(executingVar, "!=", 1);
  }

  IntVar getCostVar() {
    return costVar;
  }

  public BoolVar getExecutingVar() {
    return executingVar;
  }

  public ImmutableMap<String, Constraint> getChangeableStateVars() {
    return changeableStateVars;
  }
}
