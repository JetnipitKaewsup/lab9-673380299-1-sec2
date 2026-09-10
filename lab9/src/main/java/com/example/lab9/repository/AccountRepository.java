package com.example.lab9.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lab9.model.Account;

public interface AccountRepository extends JpaRepository<Account,Long>{

}
