package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

import java.util.Map;

/** @author Hoang Tung Dinh */
public final class StateVector {
  private final Model model;

  private final ImmutableMap<String, State> nameStateMap;

  private StateVector(Model model, ImmutableSet<StateSpec> stateSpecs) {
    this.model = model;

    final ImmutableMap.Builder<String, State> nameStateMapBuilder = ImmutableMap.builder();
    stateSpecs.forEach(
        stateSpec ->
            nameStateMapBuilder.put(
                stateSpec.name(),
                State.create(model, stateSpec.name(), NameValueBiMap.create(stateSpec.values()))));

    this.nameStateMap = nameStateMapBuilder.build();
  }

  static StateVector create(Model model, ImmutableSet<StateSpec> stateSpecs) {
    return new StateVector(model, stateSpecs);
  }

  public Constraint getHasValueConstraint(StateVectorValue stateVectorValue) {
    Constraint constraint = model.trueConstraint();

    for (final Map.Entry<String, String> stateValue :
        stateVectorValue.getStateValueMap().entrySet()) {
      constraint =
          model.and(constraint, getHasValueConstraint(stateValue.getKey(), stateValue.getValue()));
    }

    return constraint;
  }

  public Constraint getHasValueConstraint(String state, String value) {
    return nameStateMap.get(state).getHasValueConstraint(value);
  }

  public Constraint getNotHasValueConstraint(String state, String value) {
    return nameStateMap.get(state).getNotHasValueConstraint(value);
  }

  public Constraint getHasSameValueConstraint(String state, StateVector otherStateVector) {
    return nameStateMap
        .get(state)
        .getHasSameValueConstraint(otherStateVector.nameStateMap.get(state));
  }

  String getValueName(String state) {
    return nameStateMap.get(state).getValueName();
  }

  StateVectorValue getValue() {
    return StateVectorValue.create(
        nameStateMap
            .entrySet()
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Map.Entry::getKey, entry -> entry.getValue().getValueName())));
  }
}
