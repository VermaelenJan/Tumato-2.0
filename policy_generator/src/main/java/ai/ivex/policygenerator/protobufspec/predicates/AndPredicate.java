package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public class AndPredicate extends BasePredicate {
  private final Predicate left;
  private final Predicate right;

  public AndPredicate(Predicate left, Predicate right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    return model.and(
        left.getConstraint(model, stateVector, planStep),
        right.getConstraint(model, stateVector, planStep));
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector) {
    return model.and(
        left.getConstraint(model, stateVector), right.getConstraint(model, stateVector));
  }

  @Override
  public boolean getValue(StateVectorValue stateValueMap) {
    return left.getValue(stateValueMap) && right.getValue(stateValueMap);
  }

  @Override
  public boolean hasOperator(Class<Predicate> operator) {
    return getClass().isInstance(operator)
        || left.hasOperator(operator)
        || right.hasOperator(operator);
  }

  @Override
  public ImmutableSet<String> getReferencedStateVariables() {
    return ImmutableSet.<String>builder()
        .addAll(left.getReferencedStateVariables())
        .addAll(right.getReferencedStateVariables())
        .build();
  }
}
