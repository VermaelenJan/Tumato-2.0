package ai.ivex.policygenerator.protobufspec.goals;

import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import org.chocosolver.solver.Model;

public interface Goal {
  boolean attemptGoal(
      Model model,
      StateVectorValue initialStateValue,
      StateVector initialStateVector,
      PlanStep firstPlanStep,
      StateVector goalStateVector);
}
