package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.TransactionRecord; // <-- Make sure this import is correct
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, Integer> { // <-- This was the error
}