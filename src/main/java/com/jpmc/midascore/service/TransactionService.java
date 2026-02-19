package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.dto.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

@Service
public class TransactionService {

    private UserRepository userRepository;
    private TransactionRecordRepository transactionRecordRepository;

    RestTemplate restTemplate;

    @Autowired
    public TransactionService(UserRepository userRepository, TransactionRecordRepository transactionRecordRepository){
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validates and stores transactions. Rolls a transition back if something went wrong.
     */
    @Transactional
    public void processTransaction(Transaction transaction){
        // Find the sender/recipient in the database. If the ID is null, set variable to null.
        UserRecord sender;
        UserRecord recipient;

        try {
            sender = userRepository.findById(transaction.getSenderId());
        } catch (IllegalArgumentException e){
            sender = null;
        }

        try {
            recipient = userRepository.findById(transaction.getRecipientId());
        } catch (IllegalArgumentException e){
            recipient = null;
        }

        // Check if transaction is valid
        boolean isValid = isValid(transaction, sender, recipient);

        // If transaction is valid, execute it. Otherwise, don't do anything with the transaction.
        if (isValid){
            // Call incentive API only when transaction is valid
            Incentive incentive = getIncentive(transaction);

            // Update user balances in the user table of the db
            executeTransaction(transaction, sender, recipient, incentive);

            // Store valid transactions in the database
            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setSender(sender);
            transactionRecord.setRecipient(recipient);
            transactionRecord.setAmount(transaction.getAmount());
            transactionRecord.setIncentive(incentive.getAmount());
            transactionRecordRepository.save(transactionRecord);
        } else{
            return;
        }
    }

    public boolean isValid(Transaction transaction, UserRecord sender, UserRecord recipient){
        // Check for valid sender/recipient
        if (sender == null || recipient == null){
            return false;
        }

        // Check if sender has a big enough balance for the transaction
        if (sender.getBalance() < transaction.getAmount()){
            return false;
        }

        // Check that transaction amount isn't negative/zero
        if (transaction.getAmount() <= 0){
            return false;
        }

        // Return true if all aspects of transaction are valid
        return true;
    }

    public void executeTransaction(Transaction transaction, UserRecord sender, UserRecord recipient, Incentive incentive){
        // update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive.getAmount());

        // Update balances in database
        userRepository.save(sender);
        userRepository.save(recipient);
    }

    public Incentive getIncentive(Transaction transaction){
        // Fetch the incentive or print the issue while using the incentive API
        try {
            Incentive incentive = restTemplate.postForObject("http://localhost:8080/incentive", transaction, Incentive.class);

            if (incentive != null){
                return incentive;
            } else {
                System.out.println("Incentive API returned null");
                return null;
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

}
