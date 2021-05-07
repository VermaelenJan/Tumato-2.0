package ai.ivex.policygenerator;

import ai.ivex.policygenerator.cpmodel.PolicyGenerator;
import ai.ivex.policygenerator.cpmodel.Specification;
import ai.ivex.policygenerator.protobufspec.ProtobufSpecificationParser;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** @author Hoang Tung Dinh */
public final class CommandLineGenerator {

  private static final Logger logger = LoggerFactory.getLogger(CommandLineGenerator.class);

  private CommandLineGenerator() {}

  public static void main(String[] args) {
    try {
      final String protoSpecFilePath = args[0];

      logger.info("Start parsing protobuf file at " + protoSpecFilePath);

      final Path filePath = Paths.get(protoSpecFilePath);
      final String protoSpecContent = new String(Files.readAllBytes(filePath));
      final Specification specification =
          ProtobufSpecificationParser.parseSpecAndBuildModel(protoSpecContent);

      logger.info("Finish parsing protobuf message. Start generating policy.");

      final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
      final PolicyGenerator.Result result = policyGenerator.generatePolicy();

      logger.info("Finish generating policy.");

      final String outputFileName;
      final PrintWriter printWriter;

      if (result.isSuccess()) {
        outputFileName =
            FilenameUtils.removeExtension(filePath.getFileName().toString()) + "-policy.json";
        printWriter = new PrintWriter(outputFileName);
        printWriter.println(
            (new GsonBuilder())
                .setPrettyPrinting()
                .create()
                .toJson(result.getPolicy().get().getJsonObject()));

        final String xzOutputFileName =
            FilenameUtils.removeExtension(filePath.getFileName().toString()) + "-policy.xz";
        try {
          final FileOutputStream fileOutputStream = new FileOutputStream(xzOutputFileName);
          CompressorOutputStream xzOut =
              new CompressorStreamFactory()
                  .createCompressorOutputStream(CompressorStreamFactory.XZ, fileOutputStream);
          xzOut.write(
              new GsonBuilder()
                  .create()
                  .toJson(result.getPolicy().get().getCompactJsonObject())
                  .getBytes());
          xzOut.flush();
          xzOut.close();
        } catch (IOException | CompressorException e) {
          e.printStackTrace();
        }
      } else {
        outputFileName =
            FilenameUtils.removeExtension(filePath.getFileName().toString()) + "-failed-state.json";
        printWriter = new PrintWriter(outputFileName);
        printWriter.println(
            (new GsonBuilder())
                .setPrettyPrinting()
                .create()
                .toJson(result.getFailedState().get().getJsonObject()));
      }

      printWriter.close();

      logger.info("Finish writing result to " + outputFileName);

    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
