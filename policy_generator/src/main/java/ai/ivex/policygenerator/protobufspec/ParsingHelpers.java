package ai.ivex.policygenerator.protobufspec;

import ai.ivex.policygenerator.protobufspec.predicates.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

abstract class ParsingHelpers {

  static void checkActionExists(String name, ImmutableSet<String> actions, String errorMsg)
      throws Exception {
    if (!actions.contains(name)) throw new Exception(errorMsg);
  }

  static void checkStateVarExists(
      String name, ImmutableMap<String, ImmutableSet<String>> stateVarNameMap, String errorMsg)
      throws Exception {
    if (!stateVarNameMap.containsKey(name)) throw new Exception(errorMsg);
  }

  static void checkStateValueExists(
      String stateVar,
      String value,
      ImmutableMap<String, ImmutableSet<String>> stateVarNameMap,
      String errorMsg)
      throws Exception {
    if (!stateVarNameMap.get(stateVar).contains(value)) throw new Exception(errorMsg);
  }

  static Predicate getPredicateForAlias(
      String name, ImmutableMap<String, Predicate> aliases, String errorMsg) throws Exception {
    if (!aliases.containsKey(name)) throw new Exception(errorMsg);
    return aliases.get(name);
  }
}
