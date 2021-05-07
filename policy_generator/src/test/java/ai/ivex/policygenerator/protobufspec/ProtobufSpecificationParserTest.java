package ai.ivex.policygenerator.protobufspec;

import ai.ivex.policygenerator.cpmodel.Specification;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


public class ProtobufSpecificationParserTest {
  private static final String msgsDir = "src/test/resources/protobuf_msgs";

  @Test
  void testParseSdwDemoSpec() throws Exception {
    String input = new String(Files.readAllBytes(Paths.get(msgsDir, "sdw_demo_spec.buf")));

    Specification specification = ProtobufSpecificationParser.parseSpecAndBuildModel(input);

    assertThat(specification).isNotNull();


    // Basic sanity checks to see if anything obvious is missing
    assertThat(specification.stateSpecs().size()).isEqualTo(4);
    assertThat(specification.actionSpecs().size()).isEqualTo(10);
    assertThat(specification.mutuallyExclusiveActions().size()).isEqualTo(3);
    assertThat(specification.reactionRules().size()).isEqualTo(2);
    assertThat(specification.assumptions().size()).isEqualTo(0);
    assertThat(specification.goals()).isNotNull();
    assertThat(specification.maxPlanLength()).isEqualTo(10);


  }
}
