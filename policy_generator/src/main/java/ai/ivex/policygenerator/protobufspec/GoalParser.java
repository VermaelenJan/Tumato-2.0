package ai.ivex.policygenerator.protobufspec;

import ai.ivex.policygenerator.protobufspec.goals.ConditionalGoal;
import ai.ivex.policygenerator.protobufspec.goals.Goal;
import ai.ivex.policygenerator.protobufspec.goals.GoalList;
import ai.ivex.policygenerator.protobufspec.goals.SimpleGoal;
import ai.ivex.policygenerator.protobufspec.predicates.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import generated.ai.ivex.specdomain.GoalProtos;

class GoalParser {

  private static Goal parseGoal(
      GoalProtos.Goal protoGoal,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    if (protoGoal.hasSimpleGoal())
      return parseGoal(protoGoal.getSimpleGoal(), stateVarNameMap, actions, aliases);
    if (protoGoal.hasConditionalGoal())
      return parseGoal(protoGoal.getConditionalGoal(), stateVarNameMap, actions, aliases);
    if (protoGoal.hasGoalList())
      return parseGoal(protoGoal.getGoalList(), stateVarNameMap, actions, aliases);

    throw new Exception("Cannot parse goal because it is unknown: " + protoGoal.toString());
  }

  private static SimpleGoal parseGoal(
      GoalProtos.SimpleGoal simpleGoal,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate goalBody =
        PredicateParser.parsePredicate(simpleGoal.getBody(), stateVarNameMap, actions, aliases);

    return new SimpleGoal(goalBody);
  }

  private static ConditionalGoal parseGoal(
      GoalProtos.ConditionalGoal conditionalGoal,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate condition =
        PredicateParser.parsePredicate(
            conditionalGoal.getCondition(), stateVarNameMap, actions, aliases);
    Goal subGoal = parseGoal(conditionalGoal.getGoal(), stateVarNameMap, actions, aliases);

    return new ConditionalGoal(condition, subGoal);
  }

  static GoalList parseGoal(
      GoalProtos.GoalList goalList,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    ImmutableList.Builder<Goal> listBuilder = ImmutableList.builder();
    for (GoalProtos.Goal goal : goalList.getGoalsList()) {
      listBuilder.add(parseGoal(goal, stateVarNameMap, actions, aliases));
    }

    switch (goalList.getExecutionType()) {
      case PRIORITY:
        return new GoalList(listBuilder.build(), GoalList.ExecutionType.PRIORITY);
      case CONSTRAINT:
        return new GoalList(listBuilder.build(), GoalList.ExecutionType.CONSTRAINT);
    }

    throw new Exception("Unknown execution type for Goal List: " + goalList.toString());
  }
}
