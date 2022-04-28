package ai.ivex.policygenerator.cpmodel;

import org.chocosolver.solver.Model;

public interface StateRule {
  void applyConstraint(Model model, StateVector stateVector, PlanStep planStep);
}
