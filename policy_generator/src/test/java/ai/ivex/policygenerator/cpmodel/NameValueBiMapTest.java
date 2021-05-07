package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
class NameValueBiMapTest {
  @Test
  void testFuntion() {
    final ImmutableSet<String> names = ImmutableSet.of("0", "1", "2");
    final NameValueBiMap nameValueBiMap = NameValueBiMap.create(names);
    assertThat(nameValueBiMap.getName(1)).matches("1");
    assertThat(nameValueBiMap.getValue("2")).isEqualTo(2);
    assertThat(nameValueBiMap.getValues()).containsExactlyInAnyOrder(0, 1, 2);
  }
}
