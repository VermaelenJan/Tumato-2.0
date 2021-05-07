package ai.ivex.policygenerator.cpmodel;

import org.chocosolver.solver.Model;

/** @author Hoang Tung Dinh */
public interface ReactionRule {
  void applyConstraint(Model model, StateVector stateVector, PlanStep planStep);
}
