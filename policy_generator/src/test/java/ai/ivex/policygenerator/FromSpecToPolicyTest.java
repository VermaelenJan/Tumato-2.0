package ai.ivex.policygenerator;

import ai.ivex.policygenerator.cpmodel.Mapping;
import ai.ivex.policygenerator.cpmodel.PolicyGenerator;
import ai.ivex.policygenerator.cpmodel.Specification;
import ai.ivex.policygenerator.protobufspec.ProtobufSpecificationParser;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
public class FromSpecToPolicyTest {
  @Test
  void testSimpleSpec() throws Exception {
    final String specContent =
        new String(
            Files.readAllBytes(
                Paths.get(
                    getClass()
                        .getClassLoader()
                        .getResource("protobuf_msgs/test_spec.txt")
                        .getPath())));

    Specification specification = ProtobufSpecificationParser.parseSpecAndBuildModel(specContent);

    final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
    final PolicyGenerator.Result result = policyGenerator.generatePolicy();

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getPolicy().get().getMappings())
        .containsExactlyInAnyOrder(
            Mapping.create(
                ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "not_reached"),
                ImmutableSet.of("take_off")),
            Mapping.create(
                ImmutableMap.of("S_flying", "flying", "S_destination", "not_reached"),
                ImmutableSet.of("go_to_waypoint", "turn_on_led_light")),
            Mapping.create(
                ImmutableMap.of("S_flying", "flying", "S_destination", "reached"),
                ImmutableSet.of("land")),
            Mapping.create(
                ImmutableMap.of("S_flying", "on_the_ground", "S_destination", "reached"),
                ImmutableSet.of()));
  }

  @Test
  void testUavSpec() throws Exception {
    final String specContent =
        new String(
            Files.readAllBytes(
                Paths.get(
                    getClass()
                        .getClassLoader()
                        .getResource("protobuf_msgs/simple_uav_test_spec.txt")
                        .getPath())));

    Specification specification = ProtobufSpecificationParser.parseSpecAndBuildModel(specContent);

    final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
    final PolicyGenerator.Result result = policyGenerator.generatePolicy();

    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void testResourceWithoutActionUsingSpec() throws Exception {
    final String specContent =
        new String(
            Files.readAllBytes(
                Paths.get(
                    getClass()
                        .getClassLoader()
                        .getResource("protobuf_msgs/resource-without-action-using.pb.txt")
                        .getPath())));

    Specification specification = ProtobufSpecificationParser.parseSpecAndBuildModel(specContent);

    final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
    final PolicyGenerator.Result result = policyGenerator.generatePolicy();

    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void testImplicationInReactionRule() throws Exception {
    final String specContent =
        new String(
            Files.readAllBytes(
                Paths.get(
                    getClass()
                        .getClassLoader()
                        .getResource("protobuf_msgs/implication_in_reaction_rule.pb.txt")
                        .getPath())));

    Specification specification = ProtobufSpecificationParser.parseSpecAndBuildModel(specContent);

    final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
    final PolicyGenerator.Result result = policyGenerator.generatePolicy();

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getPolicy().get().getMappings())
        .containsExactlyInAnyOrder(
            Mapping.create(
                ImmutableMap.of("S1", "yes", "S2", "yes"), ImmutableSet.of("MockAction")),
            Mapping.create(ImmutableMap.of("S1", "yes", "S2", "no"), ImmutableSet.of()),
            Mapping.create(ImmutableMap.of("S1", "no", "S2", "yes"), ImmutableSet.of("MockAction")),
            Mapping.create(ImmutableMap.of("S1", "no", "S2", "no"), ImmutableSet.of("MockAction")));
  }

  @Test
  void testImplicationInGoal() throws Exception {
    final String specContent =
        new String(
            Files.readAllBytes(
                Paths.get(
                    getClass()
                        .getClassLoader()
                        .getResource("protobuf_msgs/implication_in_goal.pb.txt")
                        .getPath())));

    Specification specification = ProtobufSpecificationParser.parseSpecAndBuildModel(specContent);

    final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
    final PolicyGenerator.Result result = policyGenerator.generatePolicy();

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getPolicy().get().getMappings())
        .containsExactlyInAnyOrder(
            Mapping.create(
                ImmutableMap.of("S1", "yes", "S2", "yes"), ImmutableSet.of("MockAction")),
            Mapping.create(ImmutableMap.of("S1", "yes", "S2", "no"), ImmutableSet.of()),
            Mapping.create(ImmutableMap.of("S1", "no", "S2", "yes"), ImmutableSet.of("MockAction")),
            Mapping.create(ImmutableMap.of("S1", "no", "S2", "no"), ImmutableSet.of("MockAction")));
  }
}
