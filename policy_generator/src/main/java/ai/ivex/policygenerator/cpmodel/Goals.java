package ai.ivex.policygenerator.cpmodel;

import org.chocosolver.solver.Model;

/** @author Hoang Tung Dinh */
public interface Goals {
  void applyGoals(
      Model model,
      StateVectorValue initialStateValue,
      StateVector initialStateVector,
      PlanStep firstPlanStep,
      StateVector goalStateVector);
}
