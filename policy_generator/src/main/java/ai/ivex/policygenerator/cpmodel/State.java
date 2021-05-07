package ai.ivex.policygenerator.cpmodel;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import static com.google.common.base.Preconditions.checkArgument;

/** @author Hoang Tung Dinh */
final class State {
  private final Model model;
  private final String name;
  private final NameValueBiMap nameValueBiMap;

  private final IntVar valueVar;

  private State(Model model, String name, NameValueBiMap nameValueBiMap) {
    this.model = model;
    this.name = name;
    this.nameValueBiMap = nameValueBiMap;
    this.valueVar = model.intVar(nameValueBiMap.getValues());
  }

  static State create(Model model, String name, NameValueBiMap nameValueBiMap) {
    return new State(model, name, nameValueBiMap);
  }

  Constraint getHasValueConstraint(String value) {
    return model.arithm(valueVar, "=", nameValueBiMap.getValue(value));
  }

  Constraint getNotHasValueConstraint(String value) {
    return model.arithm(valueVar, "!=", nameValueBiMap.getValue(value));
  }

  Constraint getHasSameValueConstraint(State otherState) {
    checkArgument(name.equals(otherState.name), "States must have the same name.");
    return model.arithm(valueVar, "=", otherState.valueVar);
  }

  String getValueName() {
    return nameValueBiMap.getName(valueVar.getValue());
  }
}
