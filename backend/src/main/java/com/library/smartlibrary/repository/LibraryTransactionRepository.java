package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.LibraryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryTransactionRepository extends JpaRepository<LibraryTransaction, Long> {
    List<LibraryTransaction> findByTransactionType(String transactionType);

    List<LibraryTransaction> findTop10ByOrderByTransactionDateDesc();

    List<LibraryTransaction> findAllByOrderByTransactionDateDesc();

    @Query("SELECT t FROM LibraryTransaction t WHERE " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.transactionType) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY t.transactionDate DESC")
    List<LibraryTransaction> searchTransactions(@Param("query") String query);
}
