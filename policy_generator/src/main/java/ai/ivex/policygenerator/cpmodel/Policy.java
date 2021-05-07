package ai.ivex.policygenerator.cpmodel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Hoang Tung Dinh */
public final class Policy {

  private final ImmutableSet<StateSpec> stateSpecs;
  private final ImmutableSet<ActionSpec> actionSpecs;
  private final ImmutableSet<Mapping> mappings;

  private Policy(
      ImmutableSet<StateSpec> stateSpecs,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<Mapping> mappings) {
    this.stateSpecs = stateSpecs;
    this.actionSpecs = actionSpecs;
    this.mappings = mappings;
  }

  public static Policy create(
      ImmutableSet<StateSpec> stateSpecs,
      ImmutableSet<ActionSpec> actionSpecs,
      ImmutableSet<Mapping> mappings) {
    return new Policy(stateSpecs, actionSpecs, mappings);
  }

  @VisibleForTesting
  public ImmutableSet<Mapping> getMappings() {
    return mappings;
  }

  public JsonObject getJsonObject() {
    final JsonObject jsonPolicy = new JsonObject();

    addHeader(jsonPolicy);
    addActions(jsonPolicy);
    addStates(jsonPolicy);
    addMappings(jsonPolicy);

    return jsonPolicy;
  }

  public JsonObject getCompactJsonObject() {
    final JsonObject jsonPolicy = new JsonObject();
    jsonPolicy.addProperty("PolicyFrameworkVersion", "0.0.0");
    jsonPolicy.addProperty("Type", "compact_policy");

    final Map<String, Integer> actionIndexMap = new HashMap<>();
    int actionIndex = 0;
    final JsonArray allActions = new JsonArray();
    for (final ActionSpec actionSpec : actionSpecs) {
      allActions.add(actionSpec.name());
      actionIndexMap.put(actionSpec.name(), actionIndex);
      actionIndex++;
    }
    jsonPolicy.add("Actions", allActions);

    final Map<String, Map<String, Integer>> stateValueIndexMap = new HashMap<>();
    for (final StateSpec stateSpec : stateSpecs) {
      stateValueIndexMap.put(stateSpec.name(), new HashMap<>());
      int stateIndex = 0;
      for (final String value : stateSpec.values()) {
        stateValueIndexMap.get(stateSpec.name()).put(value, stateIndex);
        stateIndex++;
      }
    }

    Iterable<StateSpec> orderedStateSpec = new ArrayList<>(stateSpecs);
    final JsonObject stateJsonObjects = new JsonObject();
    orderedStateSpec.forEach(
        stateSpec -> {
          final JsonObject stateValueObject = new JsonObject();
          for (final String value : stateSpec.values()) {
            stateValueObject.addProperty(
                value, stateValueIndexMap.get(stateSpec.name()).get(value));
          }
          stateJsonObjects.add(stateSpec.name(), stateValueObject);
        });
    jsonPolicy.add("States", stateJsonObjects);

    final JsonArray policyJsonArray = new JsonArray();
    mappings.forEach(
        mapping -> {
          final JsonArray stateList = new JsonArray();
          orderedStateSpec.forEach(
              stateSpec ->
                  stateList.add(
                      stateValueIndexMap
                          .get(stateSpec.name())
                          .get(mapping.states().getValue(stateSpec.name()))));

          final JsonArray actionList = new JsonArray();
          mapping.actions().forEach(action -> actionList.add(actionIndexMap.get(action)));
          final JsonArray jsonMapping = new JsonArray();
          jsonMapping.add(stateList);
          jsonMapping.add(actionList);
          policyJsonArray.add(jsonMapping);
        });
    jsonPolicy.add("Policy", policyJsonArray);
    return jsonPolicy;
  }

  private void addHeader(JsonObject jsonPolicy) {
    jsonPolicy.addProperty("PolicyFrameworkVersion", "0.0.0");
    jsonPolicy.addProperty("Type", "policy");
  }

  private void addActions(JsonObject jsonPolicy) {
    final JsonArray actionJsonArray = new JsonArray();
    actionSpecs.forEach(actionSpec -> actionJsonArray.add(actionSpec.name()));
    jsonPolicy.add("Actions", actionJsonArray);
  }

  private void addStates(JsonObject jsonPolicy) {
    final JsonObject stateJsonObjects = new JsonObject();
    stateSpecs.forEach(
        stateSpec -> {
          final JsonObject valueIndexJsonObject = new JsonObject();
          final AtomicInteger counter = new AtomicInteger(0);
          stateSpec
              .values()
              .forEach(value -> valueIndexJsonObject.addProperty(value, counter.getAndIncrement()));
          stateJsonObjects.add(stateSpec.name(), valueIndexJsonObject);
        });

    jsonPolicy.add("States", stateJsonObjects);
  }

  private void addMappings(JsonObject jsonPolicy) {
    final JsonArray mappingJsonArray = new JsonArray();
    mappings.forEach(
        mapping -> {
          final JsonObject mappingJsonObject = new JsonObject();
          mapping.states().getStateValueMap().forEach(mappingJsonObject::addProperty);
          final JsonArray actionJsonArray = new JsonArray();
          mapping.actions().forEach(actionJsonArray::add);
          mappingJsonObject.add("Actions", actionJsonArray);
          mappingJsonArray.add(mappingJsonObject);
        });
    jsonPolicy.add("Policy", mappingJsonArray);
  }
}
