package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public class ValueComparisonPredicate extends BasePredicate {
  private final String stateVar;
  private final String value;

  public ValueComparisonPredicate(String stateVar, String value) {
    this.stateVar = stateVar;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getStateVar() {
    return stateVar;
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    return stateVector.getHasValueConstraint(stateVar, value);
  }

  @Override
  public Constraint getConstraint(Model model, StateVector stateVector) {
    return stateVector.getHasValueConstraint(stateVar, value);
  }

  @Override
  public boolean getValue(StateVectorValue stateValueMap) {
    return value.equals(stateValueMap.getValue(stateVar));
  }

  @Override
  public boolean hasOperator(Class<Predicate> operator) {
    return getClass().isInstance(operator);
  }

  @Override
  public ImmutableSet<String> getReferencedStateVariables() {
    return ImmutableSet.of(stateVar);
  }
}
