package com.example.lab9.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DepositTransaction")
public class DepositTransaction {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double amount;

    // FK
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // Constructor
    public DepositTransaction() {
    }

    public DepositTransaction(double amount, Account account) {
        this.amount = amount;
        this.account = account;
    }

    // Getter / Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
