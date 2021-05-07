package ai.ivex.policygenerator.protobufspec;

import ai.ivex.policygenerator.protobufspec.predicates.ActionExecutingPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.AndPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.ConstantPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.EquivalencePredicate;
import ai.ivex.policygenerator.protobufspec.predicates.ImplicationPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.NotPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.OrPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.Predicate;
import ai.ivex.policygenerator.protobufspec.predicates.ValueComparisonPredicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import generated.ai.ivex.specdomain.PredicateProtos;

class PredicateParser {

  static Predicate parsePredicate(
      PredicateProtos.Predicate protoPred,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    if (protoPred.hasAndPredicate())
      return parsePredicate(protoPred.getAndPredicate(), stateVarNameMap, actions, aliases);
    if (protoPred.hasOrPredicate())
      return parsePredicate(protoPred.getOrPredicate(), stateVarNameMap, actions, aliases);
    if (protoPred.hasImplicationPredicate())
      return parsePredicate(protoPred.getImplicationPredicate(), stateVarNameMap, actions, aliases);
    if (protoPred.hasEquivalencePredicate())
      return parsePredicate(protoPred.getEquivalencePredicate(), stateVarNameMap, actions, aliases);
    if (protoPred.hasNotPredicate())
      return parsePredicate(protoPred.getNotPredicate(), stateVarNameMap, actions, aliases);
    if (protoPred.hasConstantPredicate()) return parsePredicate(protoPred.getConstantPredicate());
    if (protoPred.hasActionExecutionPredicate())
      return parsePredicate(protoPred.getActionExecutionPredicate(), actions);
    if (protoPred.hasAliasReferencePredicate())
      return parsePredicate(protoPred.getAliasReferencePredicate(), aliases);
    if (protoPred.hasValueComparisonPredicate())
      return parsePredicate(protoPred.getValueComparisonPredicate(), stateVarNameMap);

    throw new Exception("Cannot parse predicate because it is unknown: " + protoPred.toString());
  }

  private static OrPredicate parsePredicate(
      PredicateProtos.OrPredicate predicate,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate left = parsePredicate(predicate.getLeftValue(), stateVarNameMap, actions, aliases);
    Predicate right = parsePredicate(predicate.getRightValue(), stateVarNameMap, actions, aliases);
    return new OrPredicate(left, right);
  }

  private static AndPredicate parsePredicate(
      PredicateProtos.AndPredicate predicate,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate left = parsePredicate(predicate.getLeftValue(), stateVarNameMap, actions, aliases);
    Predicate right = parsePredicate(predicate.getRightValue(), stateVarNameMap, actions, aliases);
    return new AndPredicate(left, right);
  }

  private static NotPredicate parsePredicate(
      PredicateProtos.NotPredicate predicate,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate value = parsePredicate(predicate.getValue(), stateVarNameMap, actions, aliases);
    return new NotPredicate(value);
  }

  private static ImplicationPredicate parsePredicate(
      PredicateProtos.ImplicationPredicate predicate,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate left = parsePredicate(predicate.getLeftValue(), stateVarNameMap, actions, aliases);
    Predicate right = parsePredicate(predicate.getRightValue(), stateVarNameMap, actions, aliases);
    return new ImplicationPredicate(left, right);
  }

  private static EquivalencePredicate parsePredicate(
      PredicateProtos.EquivalencePredicate predicate,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      ImmutableSet<String> actions,
      ImmutableMap<String, Predicate> aliases)
      throws Exception {
    Predicate left = parsePredicate(predicate.getLeftValue(), stateVarNameMap, actions, aliases);
    Predicate right = parsePredicate(predicate.getRightValue(), stateVarNameMap, actions, aliases);
    return new EquivalencePredicate(left, right);
  }

  private static ActionExecutingPredicate parsePredicate(
      PredicateProtos.ActionExecutionPredicate predicate, ImmutableSet<String> actions)
      throws Exception {
    ParsingHelpers.checkActionExists(
        predicate.getValue(),
        actions,
        "The action "
            + predicate.getValue()
            + " in the following predicate is unknown: "
            + predicate.toString());
    return new ActionExecutingPredicate(predicate.getValue());
  }

  private static ValueComparisonPredicate parsePredicate(
      PredicateProtos.ValueComparisonPredicate predicate,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap)
      throws Exception {
    String varName = predicate.getLeftValue();
    String value = predicate.getRightValue();

    ParsingHelpers.checkStateVarExists(
        varName,
        stateVarNameMap,
        "The state variable "
            + varName
            + " in the following predicate is unknown: "
            + predicate.toString());
    ParsingHelpers.checkStateValueExists(
        varName,
        value,
        stateVarNameMap,
        "The value "
            + value
            + " for state variable "
            + varName
            + " in the following predicate is unknown: "
            + predicate.toString());

    return new ValueComparisonPredicate(varName, value);
  }

  private static Predicate parsePredicate(
      PredicateProtos.AliasReferencePredicate predicate, ImmutableMap<String, Predicate> aliases)
      throws Exception {
    return ParsingHelpers.getPredicateForAlias(
        predicate.getValue(),
        aliases,
        "The alias "
            + predicate.getValue()
            + " in the following predicate is unknown: "
            + predicate.toString());
  }

  private static ConstantPredicate parsePredicate(PredicateProtos.ConstantPredicate predicate)
      throws Exception {
    return new ConstantPredicate(predicate.getValue());
  }
}
