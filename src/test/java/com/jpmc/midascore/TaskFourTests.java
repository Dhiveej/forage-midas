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

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskFourTests {
    static final Logger logger = LoggerFactory.getLogger(TaskFourTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;  // Inject UserRepository

    @Test
    void task_four_verifier() throws InterruptedException {
        userPopulator.populate();
        String[] transactionLines = fileLoader.loadStrings("/test_data/alskdjfh.fhdjsk");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Wait to allow Kafka consumer to process all messages
        Thread.sleep(5000);  // Increased sleep time to ensure transactions are processed

        // Fetch Wilbur user and log rounded down balance
        UserRecord wilbur = userRepository.findByUsername("wilbur");
        if (wilbur != null) {
            double roundedBalance = Math.floor(wilbur.getBalance());
            logger.info("Wilbur's balance rounded down: " + (int) roundedBalance);
        } else {
            logger.warn("User 'wilbur' not found");
        }

        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("use your debugger to find out what wilbur's balance is after all transactions are processed");
        logger.info("kill this test once you find the answer");

        // Keep the test alive for debugging if needed
        while (true) {
            Thread.sleep(20000);
            logger.info("...");
        }
    }
}
