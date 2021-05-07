package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public class ActionExecutingPredicate extends BasePredicate {
  private final String action;

  public ActionExecutingPredicate(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    return planStep.getExecutingConstraint(action);
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector) {
    throw new RuntimeException(
        "An "
            + getClass().getSimpleName()
            + " cannot be built without providing a plan step. "
            + "This usually means you have an "
            + getClass().getSimpleName()
            + " in a place where it shouldn't be.");
  }

  @Override
  public boolean hasOperator(Class<Predicate> operator) {
    return getClass().isInstance(operator);
  }

  @Override
  public boolean getValue(StateVectorValue stateValueMap) {
    throw new RuntimeException(
        "An "
            + getClass().getSimpleName()
            + " cannot be applied outside the context of the solver. "
            + "This usually means you have an "
            + getClass().getSimpleName()
            + " as condition for a priority goal.");
  }

  @Override
  public ImmutableSet<String> getReferencedStateVariables() {
    return ImmutableSet.of();
  }
}
