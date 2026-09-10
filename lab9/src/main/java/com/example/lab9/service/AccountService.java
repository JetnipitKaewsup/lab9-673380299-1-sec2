package com.example.lab9.service;

import org.springframework.stereotype.Service;

import com.example.lab9.model.Account;
import com.example.lab9.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    
    // Constructor Injection
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // add account
    public Account addAccount(Account account){
        return accountRepository.save(account);
    }

    // Find by id
    public Account findAccountById(long accountId){
        Account account = accountRepository.findById(accountId).orElseThrow(()-> new RuntimeException("Account not found " + accountId));
        return account;
    }
}