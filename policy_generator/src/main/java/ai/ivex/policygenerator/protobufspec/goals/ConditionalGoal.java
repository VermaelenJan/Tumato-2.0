package ai.ivex.policygenerator.protobufspec.goals;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import ai.ivex.policygenerator.protobufspec.predicates.Predicate;
import org.chocosolver.solver.Model;

public class ConditionalGoal implements Goal {
  private final Predicate condition;
  private final Goal body;

  public ConditionalGoal(Predicate condition, Goal body) {
    this.condition = condition;
    this.body = body;
  }

  @Override
  public boolean attemptGoal(
      Model model,
      StateVectorValue initialStateValue,
      StateVector initialStateVector,
      PlanStep firstPlanStep,
      StateVector goalStateVector) {
    if (condition.getValue(initialStateValue)) {
      body.attemptGoal(
          model, initialStateValue, initialStateVector, firstPlanStep, goalStateVector);
      return true;
    }
    return false;
  }
}
