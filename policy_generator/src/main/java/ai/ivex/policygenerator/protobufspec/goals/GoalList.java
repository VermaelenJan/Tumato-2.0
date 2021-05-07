package ai.ivex.policygenerator.protobufspec.goals;

import ai.ivex.policygenerator.cpmodel.Goals;
import ai.ivex.policygenerator.cpmodel.PlanStep;
import ai.ivex.policygenerator.cpmodel.StateVector;
import ai.ivex.policygenerator.cpmodel.StateVectorValue;
import com.google.common.collect.ImmutableList;
import org.chocosolver.solver.Model;

public class GoalList implements Goal, Goals {
  private final ImmutableList<Goal> goals;
  private final ExecutionType type;

  public GoalList(ImmutableList<Goal> goals, ExecutionType type) {
    this.goals = goals;
    this.type = type;
  }

  @Override
  public boolean attemptGoal(
      Model model,
      StateVectorValue initialStateValue,
      StateVector initialStateVector,
      PlanStep firstPlanStep,
      StateVector goalStateVector) {
    for (Goal goal : goals) {
      boolean executed =
          goal.attemptGoal(
              model, initialStateValue, initialStateVector, firstPlanStep, goalStateVector);
      if (type == ExecutionType.PRIORITY && executed) break;
    }
    return true;
  }

  @Override
  public void applyGoals(
      Model model,
      StateVectorValue initialStateValue,
      StateVector initialStateVector,
      PlanStep firstPlanStep,
      StateVector goalStateVector) {
    attemptGoal(model, initialStateValue, initialStateVector, firstPlanStep, goalStateVector);
  }

  public enum ExecutionType {
    CONSTRAINT,
    PRIORITY
  }
}
