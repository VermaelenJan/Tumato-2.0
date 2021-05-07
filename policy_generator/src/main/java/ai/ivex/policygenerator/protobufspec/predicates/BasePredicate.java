package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import org.chocosolver.solver.Model;

public abstract class BasePredicate implements Predicate {

  @Override
  public void applyConstraint(Model model, StateVector stateVector) {
    getConstraint(model, stateVector).post();
  }

  @Override
  public void applyConstraint(Model model, StateVector stateVector, PlanStep planStep) {
    getConstraint(model, stateVector, planStep).post();
  }
}
