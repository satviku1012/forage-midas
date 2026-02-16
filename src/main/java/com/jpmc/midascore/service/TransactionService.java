package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private UserRepository userRepository;
    private TransactionRecordRepository transactionRecordRepository;

    public TransactionService(UserRepository userRepository, TransactionRecordRepository transactionRecordRepository){
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    /**
     * Roll a transition back if something went wrong
     */
    @Transactional
    public void processTransaction(Transaction transaction){
        // TODO: implement business logic for incoming transactions
    }
}
