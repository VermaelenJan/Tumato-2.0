package ai.ivex.policygenerator.protobufspec.predicates;

import ai.ivex.policygenerator.cpmodel.Assumption;
import ai.ivex.policygenerator.cpmodel.Effect;
import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.Precondition;
import ai.ivex.policygenerator.cpmodel.ReactionRule;
import ai.ivex.policygenerator.cpmodel.StateRule;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

public interface Predicate extends Assumption, Effect, Precondition, ReactionRule, StateRule {

  Constraint getConstraint(Model model, StateVector stateVector, PlanStep planStep);

  boolean hasOperator(Class<Predicate> operator);

  boolean getValue(StateVectorValue stateValueMap);

  ImmutableSet<String> getReferencedStateVariables();
}
