package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public class ConstantPredicate extends BasePredicate {
  private final boolean value;

  public ConstantPredicate(boolean value) {
    this.value = value;
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector) {
    if (value) {
      return model.trueConstraint();
    } else {
      return model.falseConstraint();
    }
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    if (value) {
      return model.trueConstraint();
    } else {
      return model.falseConstraint();
    }
  }

  @Override
  public boolean getValue(StateVectorValue stateValueMap) {
    return value;
  }

  @Override
  public boolean hasOperator(Class<Predicate> operator) {
    return getClass().isInstance(operator);
  }

  @Override
  public ImmutableSet<String> getReferencedStateVariables() {
    return ImmutableSet.of();
  }
}
