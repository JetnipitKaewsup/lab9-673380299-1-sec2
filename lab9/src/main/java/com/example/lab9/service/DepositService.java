package com.example.lab9.service;

import org.springframework.stereotype.Service;

import com.example.lab9.model.Account;
import com.example.lab9.model.DepositTransaction;
import com.example.lab9.repository.AccountRepository;
import com.example.lab9.repository.DepositRepository;

import jakarta.transaction.Transactional;

@Service
public class DepositService {

    private final DepositRepository depositRepository;
    private final AccountRepository accountRepository;

    // Constructor Injection
    public DepositService(DepositRepository depositRepository, AccountRepository accountRepository) {
        this.depositRepository = depositRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void deposit(long accountId, double amount) {
        // find account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found " + accountId));

        // update balance in account
        double new_balance = account.getBalance() + amount;
        account.setBalance(new_balance);
        accountRepository.save(account);

        // save transaction
        DepositTransaction deposit = new DepositTransaction(amount, account);
        depositRepository.save(deposit);
        
        // ทดสอบ error
        //throw new RuntimeException("Test Rollback");
    }
}
