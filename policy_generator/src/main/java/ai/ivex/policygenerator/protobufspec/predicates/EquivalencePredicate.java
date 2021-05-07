package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public class EquivalencePredicate extends BasePredicate {
  private final Predicate left;
  private final Predicate right;

  public EquivalencePredicate(Predicate left, Predicate right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public void applyConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    model.ifOnlyIf(
        left.getConstraint(model, stateVector, planStep),
        right.getConstraint(model, stateVector, planStep));
  }

  @Override
  public void applyConstraint(Model model, StateVector stateVector) {
    model.ifOnlyIf(left.getConstraint(model, stateVector), right.getConstraint(model, stateVector));
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    return model.or(
        model.and(
            left.getConstraint(model, stateVector, planStep),
            right.getConstraint(model, stateVector, planStep)),
        model.and(
            model.not(left.getConstraint(model, stateVector, planStep)),
            model.not(right.getConstraint(model, stateVector, planStep))));
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector) {
    return model.or(
        model.and(left.getConstraint(model, stateVector), right.getConstraint(model, stateVector)),
        model.and(
            model.not(left.getConstraint(model, stateVector)),
            model.not(right.getConstraint(model, stateVector))));
  }

  @Override
  public boolean getValue(StateVectorValue stateValueMap) {
    return (left.getValue(stateValueMap) && right.getValue(stateValueMap))
        || (!left.getValue(stateValueMap) && !right.getValue(stateValueMap));
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
