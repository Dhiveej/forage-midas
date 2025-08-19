package com.jpmc.midascore;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;  // Inject RestTemplate

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "group-1")
    public void handleMessage(Transaction transaction) {
        logger.info("Received transaction with amount: " + transaction.getAmount());

        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {

            // Call incentive API
            Incentive incentiveResponse = null;
            try {
                incentiveResponse = restTemplate.postForObject(
                        "http://localhost:8080/incentive",
                        transaction,
                        Incentive.class
                );
            } catch (Exception e) {
                logger.error("Error calling incentive API: ", e);
                // If error, treat incentive as 0
                incentiveResponse = new Incentive();
                incentiveResponse.setAmount(0.0);
            }

            double incentiveAmount = (incentiveResponse != null && incentiveResponse.getAmount() >= 0)
                    ? incentiveResponse.getAmount() : 0.0;

            // Update balances
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

            userRepository.save(sender);
            userRepository.save(recipient);

            // Save transaction record with incentive
            TransactionRecord record = new TransactionRecord();
            record.setSender(sender);
            record.setRecipient(recipient);
            record.setAmount(transaction.getAmount());
            record.setIncentive(incentiveAmount);
            // Optionally set memo, currency if applicable

            transactionRepository.save(record);

        } else {
            logger.warn("Invalid transaction discarded: " + transaction);
        }
    }
}
