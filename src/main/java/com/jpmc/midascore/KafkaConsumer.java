package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final TransactionService transactionService;

    @Autowired
    public KafkaConsumer(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction){
        try{
            transactionService.processTransaction(transaction);
            System.out.println("Successful in processing transaction: " + transaction);
        } catch (Exception e){
            System.out.println("Error processing transaction: " + transaction);
        }
    }
}
