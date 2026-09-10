package com.example.lab9.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lab9.model.Account;
import com.example.lab9.service.AccountService;
import com.example.lab9.service.DepositService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final DepositService depositService;

    // Constructor Injection
    public AccountController(AccountService accountService, DepositService depositService) {
        this.accountService = accountService;
        this.depositService = depositService;
    }

    // Add Account
    @PostMapping
    public Account addAccount(@RequestBody Account account) {
        return accountService.addAccount(account);
    }

    // Deposit
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Map<String,String>> deposit(@PathVariable long id,@RequestBody Map<String,Double> request){
        depositService.deposit(id, request.get("amount"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Deposit succesful"));
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable long id){
        Account account = accountService.findAccountById(id);
        return account;
    }

}
