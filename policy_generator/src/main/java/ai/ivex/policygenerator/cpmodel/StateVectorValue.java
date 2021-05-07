package ai.ivex.policygenerator.cpmodel;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import java.util.Objects;

/** @author Hoang Tung Dinh */
public final class StateVectorValue {
  private final ImmutableMap<String, String> stateValueMap;

  private StateVectorValue(ImmutableMap<String, String> stateValueMap) {
    this.stateValueMap = stateValueMap;
  }

  static StateVectorValue create(ImmutableMap<String, String> stateValueMap) {
    return new StateVectorValue(stateValueMap);
  }

  public ImmutableMap<String, String> getStateValueMap() {
    return stateValueMap;
  }

  public String getValue(String stateName) {
    return stateValueMap.get(stateName);
  }

  public JsonObject getJsonObject() {
    final JsonObject jsonObject = new JsonObject();
    stateValueMap.forEach(jsonObject::addProperty);
    return jsonObject;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StateVectorValue that = (StateVectorValue) o;
    return Objects.equals(stateValueMap, that.stateValueMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stateValueMap);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("stateValueMap", stateValueMap).toString();
  }
}
