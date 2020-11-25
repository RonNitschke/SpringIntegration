package com.example.demo;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.DelayerEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.file.dsl.Files;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test the {@link org.springframework.integration.dsl#delay} with a file IntegrationFlowRegistration.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestDelayFileIntegrationFlowRegistration {

  private static final Logger LOG = LoggerFactory.getLogger("TestDelayFileIntegrationFlowRegistration");

  static File pollDirectory2 = new File("/tmp/polltest2");


  /**
   * Configure the {@link IntegrationFlowRegistration}.
   */
  @Configuration
  @EnableIntegration
  public static class ContextConfiguration {


    private static File newFile2 = new File(pollDirectory2, "newFile2.txt");

    static {
      try {
        if (!newFile2.exists()) {
          newFile2.mkdirs();
        }
        newFile2.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Autowired
    private IntegrationFlowContext flowContext;

    /**
     * Setup the {@link IntegrationFlowRegistration}.
     *
     * @return the {@link IntegrationFlowRegistration}.
     */
    @Bean
    public IntegrationFlowRegistration fileFlow2() {
      IntegrationFlow flow = IntegrationFlows
          .from(Files.inboundAdapter(pollDirectory2), e -> e.poller(Pollers.fixedDelay(100)))
          .delay("delayer",
              (DelayerEndpointSpec e) -> e.defaultDelay(1000))
          .handle(File.class, (file, headers) -> {
            LOG.info("**** Retry handling for " + file);
            if (file.exists()) {
              LOG.info("**** file exists");
              throw new RuntimeException();
              // readMessageRetryHandler.handleMessage(nodeConfig, file);
            } else {
              LOG.info("*** No File");
            }
            return null;
          })
          .get();
      return this.flowContext.registration(flow).register();
    }

  }


  /**
   * Test the file flow.
   *
   * @throws Exception
   */
  @Test
  public void testFileFlow() throws Exception {

    (new File(pollDirectory2, "yetAnotherNewFile.txt")).createNewFile();

    Thread.sleep(5000);
  }

}
