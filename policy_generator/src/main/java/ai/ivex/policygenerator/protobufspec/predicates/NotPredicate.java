package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public class NotPredicate extends BasePredicate {
  private final Predicate predicate;

  public NotPredicate(Predicate predicate) {
    this.predicate = predicate;
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    return model.not(predicate.getConstraint(model, stateVector, planStep));
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector) {
    return model.not(predicate.getConstraint(model, stateVector));
  }

  @Override
  public boolean getValue(StateVectorValue stateValueMap) {
    return !predicate.getValue(stateValueMap);
  }

  @Override
  public boolean hasOperator(Class<Predicate> operator) {
    return getClass().isInstance(operator) || predicate.hasOperator(operator);
  }

  @Override
  public ImmutableSet<String> getReferencedStateVariables() {
    return predicate.getReferencedStateVariables();
  }
}
