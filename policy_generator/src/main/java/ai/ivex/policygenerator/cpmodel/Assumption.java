package ai.ivex.policygenerator.cpmodel;

import org.chocosolver.solver.Model;

/** @author Hoang Tung Dinh */
public interface Assumption {
  void applyConstraint(Model model, StateVector stateVector);
}
