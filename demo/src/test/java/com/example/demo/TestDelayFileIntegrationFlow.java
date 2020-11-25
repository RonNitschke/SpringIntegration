package com.example.demo;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.DelayerEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files; 
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test the {@link org.springframework.integration.dsl#delay} with a file IntegrationFlow.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestDelayFileIntegrationFlow {

  private static final Logger LOG = LoggerFactory.getLogger("TestDelayFileIntegrationFlow");

  static File pollDirectory = new File("/tmp/polltest");

  /**
   * Configure the {@link IntegrationFlow}.
   */
  @Configuration
  @EnableIntegration
  public static class ContextConfiguration {

    private static File newFile = new File(pollDirectory, "newFile.txt");

    static {
      try {
        if (!newFile.exists()) {
          newFile.mkdirs();
        }
        newFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /**
     * Setup the {@link IntegrationFlow}.
     *
     * @return the {@link IntegrationFlow}.
     */
    @Bean
    public IntegrationFlow fileFlow() {
      IntegrationFlow flow = IntegrationFlows
          .from(Files.inboundAdapter(pollDirectory), e -> e.poller(Pollers.fixedDelay(100)))
          .delay("delayer",
              (DelayerEndpointSpec e) -> e.defaultDelay(1000))
          .handle(File.class, (file, headers) -> {
            LOG.info("**** Retry handling for " + file);
            if (file.exists()) {
              LOG.info("**** file exists");
              throw new RuntimeException();
            } else {
              LOG.info("*** No File");
            }
            return null;
          })
          .get();
      return flow;
    }

  }

  /**
   * Test the file flow.
   *
   * @throws Exception
   */
  @Test
  public void testFileFlow() throws Exception {

    (new File(pollDirectory, "anotherNewFile.txt")).createNewFile();

    Thread.sleep(5000);
  }

}
