package ai.ivex.policygenerator.protobufspec.goals;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import ai.ivex.policygenerator.protobufspec.predicates.Predicate;
import org.chocosolver.solver.Model;

public class SimpleGoal implements Goal {
  private final Predicate body;

  public SimpleGoal(Predicate body) {
    this.body = body;
  }

  @Override
  public boolean attemptGoal(
      Model model,
      StateVectorValue initialStateValue,
      StateVector initialStateVector,
      PlanStep firstPlanStep,
      StateVector goalStateVector) {
    body.getConstraint(model, goalStateVector, firstPlanStep).post();
    return true;
  }
}
