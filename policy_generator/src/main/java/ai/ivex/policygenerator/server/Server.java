package ai.ivex.policygenerator.server;

import ai.ivex.policygenerator.cpmodel.PolicyGenerator;
import ai.ivex.policygenerator.cpmodel.Specification;
import ai.ivex.policygenerator.protobufspec.ProtobufSpecificationParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** @author Hoang Tung Dinh */
public final class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private Server() {}

  public static void main(String[] args) {
    logger.info("Start policy generator server.");
    final Lock lock = new ReentrantLock();

    Spark.port(8085);

    Spark.post(
        "/generate",
        (request, response) -> {
          logger.info("Get a policy generator request.");
          if (!lock.tryLock()) {
            response.status(503);
            return response.body();
          }

          final JsonObject body = new JsonObject();
          response.type("application/json");

          try {
            final String protobufSpec = request.body();
            final Specification specification =
                ProtobufSpecificationParser.parseSpecAndBuildModel(protobufSpec);

            logger.info("Finish parsing request. Start generating policy.");

            final PolicyGenerator policyGenerator = PolicyGenerator.create(specification);
            final PolicyGenerator.Result result = policyGenerator.generatePolicy();

            logger.info("Finish generating policy.");

            if (result.isSuccess()) {
              body.addProperty("status", "succeeded");
              body.add("generated_policy", result.getPolicy().get().getJsonObject());
            } else {
              body.addProperty("status", "failed");
              body.add("failed_state", result.getFailedState().get().getJsonObject());
            }
          } catch (Exception e) {
            response.status(500);
            body.addProperty("error_message", e.getMessage());
          } finally {
            lock.unlock();
          }
          return (new Gson()).toJson(body);
        });
  }
}
