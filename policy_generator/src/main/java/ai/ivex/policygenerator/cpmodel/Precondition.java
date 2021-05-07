package ai.ivex.policygenerator.cpmodel;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

/** @author Hoang Tung Dinh */
public interface Precondition {
  Constraint getConstraint(Model model, StateVector stateVector);
}
