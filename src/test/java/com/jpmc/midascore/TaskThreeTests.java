package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskThreeTests {
    private static final Logger logger = LoggerFactory.getLogger(TaskThreeTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    @Test
    void task_three_verifier() throws InterruptedException {
        // Populate Users
        userPopulator.populate();

        // Load transactions from file
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");

        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Wait for Kafka consumer processing
        Thread.sleep(5000);

        logger.info("Attempting to fetch Waldorf's balance now...");

        // Fetch and log Waldorf's balance
        UserRecord waldorf = userRepository.findByUsername("waldorf");
        if (waldorf != null) {
            double roundedBalance = Math.floor(waldorf.getBalance());
            logger.info("Waldorf's balance rounded down: " + (int) roundedBalance);
        } else {
            logger.warn("User 'waldorf' not found");
        }

        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("Use your debugger to inspect further if needed.");
        logger.info("Kill this test once you find your answer.");

        // Keep test alive for debugging if needed
        while (true) {
            Thread.sleep(20000);
            logger.info("...");
        }
    }
}
