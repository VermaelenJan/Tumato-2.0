package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/** @author Hoang Tung Dinh */
final class NameValueBiMap {
  private final Object2IntMap<String> nameValueMap = new Object2IntOpenHashMap<>();
  private final Int2ObjectMap<String> valueNameMap = new Int2ObjectOpenHashMap<>();

  private NameValueBiMap(ImmutableSet<String> names) {
    int counter = 0;

    for (final String name : names) {
      nameValueMap.put(name, counter);
      valueNameMap.put(counter, name);
      counter++;
    }
  }

  static NameValueBiMap create(ImmutableSet<String> names) {
    return new NameValueBiMap(names);
  }

  int getValue(String name) {
    return nameValueMap.getInt(name);
  }

  String getName(int value) {
    return valueNameMap.get(value);
  }

  int[] getValues() {
    return nameValueMap.values().toIntArray();
  }
}
